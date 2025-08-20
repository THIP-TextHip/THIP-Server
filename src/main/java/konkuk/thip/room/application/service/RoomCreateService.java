package konkuk.thip.room.application.service;

import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.room.application.port.in.RoomCreateUseCase;
import konkuk.thip.room.application.port.in.dto.RoomCreateCommand;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.domain.RoomParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomCreateService implements RoomCreateUseCase {

    private final RoomCommandPort roomCommandPort;
    private final RoomParticipantCommandPort roomParticipantCommandPort;
    private final BookCommandPort bookCommandPort;
    private final BookApiQueryPort bookApiQueryPort;

    @Override
    @Transactional
    public Long createRoom(RoomCreateCommand command, Long userId) {
        // 1. Category 생성
        Category category = Category.from(command.category());

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
                category
        );
        Long savedRoomId = roomCommandPort.save(room);

        // 4. 방장 RoomParticipant 생성 및 DB save
        RoomParticipant roomParticipant = RoomParticipant.hostWithoutId(userId, savedRoomId);
        roomParticipantCommandPort.save(roomParticipant);

        return savedRoomId;
    }

    private Long resolveBookAndEnsurePage(String isbn) {
        return bookCommandPort.findByIsbn(isbn)
                .map(book -> {
                    if (!book.hasPageCount()) {
                        updateBookPageCount(book);
                    }
                    return book.getId();
                })
                .orElseGet(() -> saveNewBookWithPageCount(isbn));
    }

    private void updateBookPageCount(Book book) {
        Integer pageCount = bookApiQueryPort.findPageCountByIsbn(book.getIsbn());
        book.changePageCount(pageCount);
        bookCommandPort.updateForPageCount(book);
    }

    private Long saveNewBookWithPageCount(String isbn) {
        Book loaded = bookApiQueryPort.loadBookWithPageByIsbn(isbn);
        return bookCommandPort.save(loaded);
    }
}
