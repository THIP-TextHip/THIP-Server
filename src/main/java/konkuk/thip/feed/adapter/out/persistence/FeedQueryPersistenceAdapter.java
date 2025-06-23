package konkuk.thip.feed.adapter.out.persistence;

import konkuk.thip.feed.adapter.out.mapper.FeedMapper;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedQueryPersistenceAdapter implements FeedQueryPort {

    private final FeedJpaRepository jpaRepository;
    private final FeedMapper userMapper;

}
