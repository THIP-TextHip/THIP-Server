package konkuk.thip.book.application.port.out;

import konkuk.thip.book.application.port.out.dto.BookQueryDto;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;

public interface BookQueryPort {

    boolean existsSavedBookByUserIdAndBookId(Long userId, Long bookId);

    boolean existsBookByIsbn(String isbn);

    CursorBasedList<BookQueryDto> findSavedBooksBySavedAt(Long userId, Cursor cursor);

    CursorBasedList<BookQueryDto> findJoiningRoomsBooksByRoomPercentage(Long userId, Cursor cursor);

    Set<Long> findUnusedBookIds();
}
