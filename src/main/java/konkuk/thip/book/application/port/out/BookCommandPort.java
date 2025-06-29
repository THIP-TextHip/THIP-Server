package konkuk.thip.book.application.port.out;


import konkuk.thip.book.domain.Book;

public interface BookCommandPort {

    Book findByIsbn(String isbn);

}