package konkuk.thip.book.application.port.out;


import konkuk.thip.book.domain.Book;

import java.util.Optional;

public interface BookCommandPort {

    Book findByIsbn(String isbn);
    Long save(Book book);
}