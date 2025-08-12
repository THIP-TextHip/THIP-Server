package konkuk.thip.book.application.port.out;


import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.code.ErrorCode;

import java.util.Optional;

public interface BookCommandPort {

    Optional<Book> findByIsbn(String isbn);

    default Book getByIsbnOrThrow(String isbn){
        return findByIsbn(isbn)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BOOK_NOT_FOUND));
    }

    Long save(Book book);

    Book findById(Long id);

    void updateForPageCount(Book book);

    Book findBookByRoomId(Long roomId);

    void saveSavedBook(Long userId, Long bookId);
    void deleteSavedBook(Long userId, Long bookId);

}
