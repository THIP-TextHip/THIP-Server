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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class RoomCreateService implements RoomCreateUseCase {

    private final RoomCommandPort roomCommandPort;
    private final RoomParticipantCommandPort roomParticipantCommandPort;
    private final BookCommandPort bookCommandPort;
    private final BookApiQueryPort bookApiQueryPort;
    private final TransactionTemplate transactionTemplate;

    @Override
    public Long createRoom(RoomCreateCommand command, Long userId) {
        // 1. Category 생성
        Category category = Category.from(command.category());

        // 2. Book 찾기, 없으면 외부 API로 로드 및 저장 (트랜잭션 밖에서 수행)
        Long bookId = resolveBookAndEnsurePage(command.isbn());

        // 3. Room + RoomParticipant 저장 (단일 트랜잭션으로 원자성 보장)
        return transactionTemplate.execute(status -> {
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

            RoomParticipant roomParticipant = RoomParticipant.hostWithoutId(userId, savedRoomId);
            roomParticipantCommandPort.save(roomParticipant);

            return savedRoomId;
        });
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
        // 알라딘 API 실패 시 null 반환 (Discord 알림은 어댑터에서 처리)
        Integer pageCount = bookApiQueryPort.findPageCountByIsbn(book.getIsbn());
        if (pageCount == null) return;

        book.changePageCount(pageCount);
        bookCommandPort.updateForPageCount(book);
    }

    private Long saveNewBookWithPageCount(String isbn) {
        Book loaded = bookApiQueryPort.loadBookWithPageByIsbn(isbn);
        try {
            return bookCommandPort.save(loaded);
        } catch (DataIntegrityViolationException e) {
            // 동일 ISBN 동시 요청으로 이미 저장된 경우 → 기존 Book ID 반환
            return bookCommandPort.findByIsbn(isbn)
                    .map(Book::getId)
                    .orElseThrow(() -> e);
        }
    }
}
