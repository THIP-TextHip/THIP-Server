package konkuk.thip.domain.feed.adapter.out.persistence;

import konkuk.thip.domain.feed.adapter.out.mapper.FeedMapper;
import konkuk.thip.domain.feed.application.port.out.FeedCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedCommandPersistenceAdapter implements FeedCommandPort {

    private final FeedJpaRepository jpaRepository;
    private final FeedMapper userMapper;

}
