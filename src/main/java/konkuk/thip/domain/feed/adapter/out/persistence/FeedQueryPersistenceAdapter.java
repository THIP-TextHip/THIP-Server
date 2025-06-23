package konkuk.thip.domain.feed.adapter.out.persistence;

import konkuk.thip.domain.feed.adapter.out.mapper.FeedMapper;
import konkuk.thip.domain.feed.application.port.out.FeedQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedQueryPersistenceAdapter implements FeedQueryPort {

    private final FeedJpaRepository jpaRepository;
    private final FeedMapper userMapper;

}
