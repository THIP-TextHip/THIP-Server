package konkuk.thip.book.adapter.out.persistence;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.mapper.BookMapper;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static konkuk.thip.common.exception.code.ErrorCode.BOOK_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class BookCommandPersistenceAdapter implements BookCommandPort {

    private final BookJpaRepository bookJpaRepository;
    private final BookMapper bookMapper;

    @Override
    public Book findByIsbn(String isbn) {
        BookJpaEntity bookJpaEntity = bookJpaRepository.findByIsbn(isbn).orElseThrow(
                () -> new EntityNotFoundException(BOOK_NOT_FOUND));
        return bookMapper.toDomainEntity(bookJpaEntity);
    }


    @Override
    public Long save(Book book) {
        BookJpaEntity bookJpaEntity = bookMapper.toJpaEntity(book);
        return bookJpaRepository.save(bookJpaEntity).getBookId();
    }
}
