package konkuk.thip.feed.adapter.out.persistence.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.feed.adapter.out.jpa.QFeedJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class FeedQueryRepositoryImpl implements FeedQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Set<Long> findUserIdsByBookId(Long bookId) {
        QFeedJpaEntity feed = QFeedJpaEntity.feedJpaEntity;
        Set<Long> userIds = new HashSet<>(
                jpaQueryFactory
                        .select(feed.userJpaEntity.userId)
                        .distinct()
                        .from(feed)
                        .where(feed.bookJpaEntity.bookId.eq(bookId))
                        .fetch()
        );
        return userIds;
    }
}
