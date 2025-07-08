package konkuk.thip.room.application.service;

import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.room.application.port.in.RoomCreateUseCase;
import konkuk.thip.room.application.port.in.dto.RoomCreateCommand;
import konkuk.thip.room.application.port.out.CategoryCommandPort;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.domain.Category;
import konkuk.thip.room.domain.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomCreateService implements RoomCreateUseCase {

    private final RoomCommandPort roomCommandPort;
    private final CategoryCommandPort categoryCommandPort;
    private final BookCommandPort bookCommandPort;
    private final BookApiQueryPort bookApiQueryPort;

    @Override
    @Transactional
    public Long createRoom(RoomCreateCommand command) {
        // 1. Category 찾기
        Category category = categoryCommandPort.findByValue(command.category());

        // 2. Book 찾기, 없으면 Book 로드 및 저장
        Long bookId = resolveBookAndEnsurePage(command.isbn());

        // 3. Room 생성 및 저장
        Room room = Room.withoutId(
                command.roomName(),
                command.description(),
                command.isPublic(),
                command.password(),
                command.progressStartDate(),
                command.progressEndDate(),
                command.recruitCount(),
                bookId,
                category.getId()
        );

        return roomCommandPort.save(room);
    }

    private Long resolveBookAndEnsurePage(String isbn) {
        try {
            Book existing = bookCommandPort.findByIsbn(isbn);
            if (!existing.hasPageCount()) {
                updateBookPageCount(existing);
            }
            return existing.getId();
        } catch (EntityNotFoundException e) {
            return saveNewBookWithPageCount(isbn);
        }
    }

    private void updateBookPageCount(Book book) {
        Integer pageCount = bookApiQueryPort.findPageCountByIsbn(book.getIsbn());
        Book updated = book.changePageCount(pageCount);
        bookCommandPort.updateForPageCount(updated);
    }

    private Long saveNewBookWithPageCount(String isbn) {
        Book loaded = bookApiQueryPort.loadBookWithPageByIsbn(isbn);
        return bookCommandPort.save(loaded);
    }
}
