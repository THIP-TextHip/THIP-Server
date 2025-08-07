package konkuk.thip.book.application.port.out;

import konkuk.thip.book.domain.Book;

import java.util.Set;

public interface BookQueryPort {

    boolean existsSavedBookByUserIdAndBookId(Long userId, Long bookId);

    Set<Book> findSavedBooksByUserId(Long userId);

    Set<Book> findJoiningRoomsBooksByUserId(Long userId);
}
