package konkuk.thip.book.adapter.out.persistence;

import konkuk.thip.book.adapter.out.mapper.BookMapper;
import konkuk.thip.book.application.port.out.BookCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookCommandPersistenceAdapter implements BookCommandPort {

    private final BookJpaRepository jpaRepository;
    private final BookMapper userMapper;

}
