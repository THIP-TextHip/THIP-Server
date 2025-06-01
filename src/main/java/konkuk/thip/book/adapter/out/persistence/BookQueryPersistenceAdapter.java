package konkuk.thip.book.adapter.out.persistence;

import konkuk.thip.book.adapter.out.mapper.BookMapper;
import konkuk.thip.book.application.port.out.BookQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookQueryPersistenceAdapter implements BookQueryPort {

    private final BookJpaRepository jpaRepository;
    private final BookMapper userMapper;

}
