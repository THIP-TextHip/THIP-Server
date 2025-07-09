package konkuk.thip.book.application.port.out;


import konkuk.thip.book.domain.Book;

public interface BookCommandPort {

    Book findByIsbn(String isbn);

    Long save(Book book);

    Book findById(Long id);

    void updateForPageCount(Book book);
}
