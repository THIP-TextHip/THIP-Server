package konkuk.thip.book.adapter.out.persistence;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.mapper.BookMapper;
import konkuk.thip.book.application.port.out.BookQueryPort;
import konkuk.thip.book.domain.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BookQueryPersistenceAdapter implements BookQueryPort {

    private final BookJpaRepository bookJpaRepository;
    private final BookMapper bookMapper;

    @Override
    public List<Book> findByIsbnIn(List<String> isbnList) {
        List<BookJpaEntity> entities = bookJpaRepository.findByIsbnIn(isbnList);
        return entities.stream()
                .map(bookMapper::toDomainEntity)
                .collect(Collectors.toList());
    }
}
