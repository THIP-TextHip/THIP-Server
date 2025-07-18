package konkuk.thip.feed.adapter.out.persistence.repository;

import java.util.Set;

public interface FeedQueryRepository {
    Set<Long> findUserIdsByBookId(Long bookId);
}