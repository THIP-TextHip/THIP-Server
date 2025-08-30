package konkuk.thip.book.adapter.out.persistence;

import konkuk.thip.book.adapter.out.mapper.BookMapper;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.book.adapter.out.persistence.repository.SavedBookJpaRepository;
import konkuk.thip.book.application.port.out.BookQueryPort;
import konkuk.thip.book.application.port.out.dto.BookQueryDto;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookQueryPersistenceAdapter implements BookQueryPort {

    private final UserJpaRepository userJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final SavedBookJpaRepository savedBookJpaRepository;
    private final BookMapper bookMapper;

    @Override
    public boolean existsSavedBookByUserIdAndBookId(Long userId, Long bookId) {
        return savedBookJpaRepository.existsByUserIdAndBookId(userId, bookId);
    }

    @Override
    public boolean existsBookByIsbn(String isbn) {
        return bookJpaRepository.existsByIsbn(isbn);
    }

    @Override
    public CursorBasedList<BookQueryDto> findSavedBooksBySavedAt(Long userId, Cursor cursor) {
        LocalDateTime lastSavedAt = cursor.isFirstRequest() ? null : cursor.getLocalDateTime(0);
        int pageSize = cursor.getPageSize();

        List<BookQueryDto> dtos = bookJpaRepository.findSavedBooksBySavedAt(userId, lastSavedAt, pageSize);

        return CursorBasedList.of(dtos, pageSize, dto -> {
            Cursor nextCursor = new Cursor(List.of(dto.savedCreatedAt().toString()));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CursorBasedList<BookQueryDto> findJoiningRoomsBooksByRoomPercentage(Long userId, Cursor cursor) {
        Double lastRoomPercentage = cursor.isFirstRequest() ? null : cursor.getDouble(0);
        Long lastBookId = cursor.isFirstRequest() ? null : cursor.getLong(1);
        int pageSize = cursor.getPageSize();

        List<BookQueryDto> dtos = bookJpaRepository.findJoiningRoomsBooksByRoomPercentage(userId, lastRoomPercentage, lastBookId, pageSize);

        return CursorBasedList.of(dtos, pageSize, dto -> {
            Cursor nextCursor = new Cursor(List.of(
                    dto.roomPercentage().toString(), // 내림차순 필드, 정렬순서 1
                    dto.bookId().toString() // 고유 ID, 중복 방지용
            ));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public Set<Long> findUnusedBookIds() {
        return bookJpaRepository.findUnusedBookIds();
    }

}
