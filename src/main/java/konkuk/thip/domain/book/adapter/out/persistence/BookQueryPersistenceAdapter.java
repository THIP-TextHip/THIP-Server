package konkuk.thip.domain.book.adapter.out.persistence;

import konkuk.thip.domain.book.adapter.out.mapper.BookMapper;
import konkuk.thip.domain.book.application.port.out.BookQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookQueryPersistenceAdapter implements BookQueryPort {

    private final BookJpaRepository jpaRepository;
    private final BookMapper userMapper;

}
