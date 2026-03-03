package konkuk.thip.book.application.port.out;

import konkuk.thip.book.application.port.out.dto.BookQueryDto;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;

import java.util.List;
import java.util.Set;

public interface BookQueryPort {

    boolean existsSavedBookByUserIdAndBookId(Long userId, Long bookId);

    boolean existsBookByIsbn(String isbn);

    CursorBasedList<BookQueryDto> findSavedBooksBySavedAt(Long userId, Cursor cursor);

    CursorBasedList<BookQueryDto> findJoiningRoomsBooksByRoomPercentage(Long userId, Cursor cursor);

    Set<Long> findUnusedBookIds();

    // room과 연관된 book 중 pageCount가 null이고 아직 포기하지 않은 book 전체 조회 (스케줄러용)
    List<Book> findBooksWithNullPageCountLinkedToRooms();
}
