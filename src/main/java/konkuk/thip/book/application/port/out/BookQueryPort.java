package konkuk.thip.book.application.port.out;

import konkuk.thip.book.domain.Book;

import java.util.List;

public interface BookQueryPort {

    boolean existsSavedBookByUserIdAndBookId(Long userId, Long bookId);

    boolean existsBookByIsbn(String isbn);

    List<Book> findSavedBooksByUserId(Long userId);

    List<Book> findJoiningRoomsBooksByUserId(Long userId);
}
