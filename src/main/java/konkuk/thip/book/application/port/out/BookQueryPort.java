package konkuk.thip.book.application.port.out;

import konkuk.thip.book.domain.Book;

import java.util.List;

public interface BookQueryPort {
    List<Book> findByIsbnIn(List<String> isbnList);
}
