package konkuk.thip.book.adapter.out.persistence;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.mapper.BookMapper;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.BOOK_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class BookCommandPersistenceAdapter implements BookCommandPort {

    private final BookJpaRepository bookJpaRepository;
    private final BookMapper bookMapper;

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return bookJpaRepository.findByIsbn(isbn)
                .map(bookMapper::toDomainEntity);
    }

    @Override
    public Book findById(Long id) {
        BookJpaEntity bookJpaEntity = bookJpaRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(BOOK_NOT_FOUND)
        );

        return bookMapper.toDomainEntity(bookJpaEntity);
    }
}
