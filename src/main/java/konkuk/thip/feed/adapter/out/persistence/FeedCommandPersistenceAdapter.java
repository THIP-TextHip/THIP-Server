package konkuk.thip.feed.adapter.out.persistence;

import konkuk.thip.feed.adapter.out.mapper.FeedMapper;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedCommandPersistenceAdapter implements FeedCommandPort {

    private final FeedJpaRepository jpaRepository;
    private final FeedMapper userMapper;

}
