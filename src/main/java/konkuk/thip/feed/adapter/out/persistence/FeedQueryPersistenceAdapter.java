package konkuk.thip.feed.adapter.out.persistence;

import konkuk.thip.feed.adapter.out.mapper.FeedMapper;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FeedQueryPersistenceAdapter implements FeedQueryPort {

    private final FeedJpaRepository feedJpaRepository;
    private final FeedMapper feedMapper;

    @Override
    public Set<Long> findUserIdsByBookId(Long bookId) {
        return feedJpaRepository.findUserIdsByBookId(bookId);
    }
}
