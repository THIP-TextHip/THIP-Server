package konkuk.thip.post.adapter.out.persistence.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.feed.adapter.out.jpa.QFeedJpaEntity;
import konkuk.thip.post.adapter.out.jpa.QPostJpaEntity;
import konkuk.thip.post.application.port.out.dto.PostQueryDto;
import konkuk.thip.post.application.port.out.dto.QPostQueryDto;
import konkuk.thip.roompost.adapter.out.jpa.QRecordJpaEntity;
import konkuk.thip.roompost.adapter.out.jpa.QVoteJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.querydsl.jpa.JPAExpressions.treat;
import static konkuk.thip.post.domain.PostType.*;

@Repository
@RequiredArgsConstructor
public class PostQueryRepositoryImpl implements PostQueryRepository {

    private final QPostJpaEntity post = QPostJpaEntity.postJpaEntity;
    private final QFeedJpaEntity feed = QFeedJpaEntity.feedJpaEntity;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public PostQueryDto getPostQueryDtoByFeedId(Long feedId) {
        return jpaQueryFactory
                .select(new QPostQueryDto(
                        feed.postId,
                        feed.userJpaEntity.userId,
                        feed.dtype,
                        Expressions.nullExpression(),
                        Expressions.nullExpression()
                ))
                .from(feed)
                .where(feed.postId.eq(feedId))
                .where(feed.dtype.eq(FEED.getType()))
                .fetchOne();
    }

    @Override
    public PostQueryDto getPostQueryDtoByRecordId(Long recordId) {
        return jpaQueryFactory
                .select(new QPostQueryDto(
                        post.postId,
                        post.userJpaEntity.userId,
                        post.dtype,
                        treat(post, QRecordJpaEntity.class).page,    // Record의 page
                        treat(post, QRecordJpaEntity.class).roomJpaEntity.roomId   // Record의 roomId
                ))
                .from(post)
                .where(post.postId.eq(recordId))
                .where(post.dtype.eq(RECORD.getType()))
                .fetchOne();
    }

    @Override
    public PostQueryDto getPostQueryDtoByVoteId(Long voteId) {
        return jpaQueryFactory
                .select(new QPostQueryDto(
                        post.postId,
                        post.userJpaEntity.userId,
                        post.dtype,
                        treat(post, QVoteJpaEntity.class).page,    // Vote의 page
                        treat(post, QVoteJpaEntity.class).roomJpaEntity.roomId    // Vote의 roomId
                ))
                .from(post)
                .where(post.postId.eq(voteId))
                .where(post.dtype.eq(VOTE.getType()))
                .fetchOne();
    }
}
