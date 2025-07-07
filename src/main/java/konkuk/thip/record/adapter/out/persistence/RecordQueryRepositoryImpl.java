package konkuk.thip.record.adapter.out.persistence;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.comment.adapter.out.jpa.QCommentJpaEntity;
import konkuk.thip.post.adapter.out.jpa.PostJpaEntity;
import konkuk.thip.post.adapter.out.jpa.QPostJpaEntity;
import konkuk.thip.post.adapter.out.jpa.QPostLikeJpaEntity;
import konkuk.thip.record.adapter.out.jpa.QRecordJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.QVoteJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.querydsl.core.types.dsl.Expressions.numberTemplate;

@Repository
@RequiredArgsConstructor
public class RecordQueryRepositoryImpl implements RecordQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<PostJpaEntity> findPostsByRoom(Long roomId, String type, String sort, Integer pageStart, Integer pageEnd, Long userId, Pageable pageable) {
        QPostJpaEntity post = QPostJpaEntity.postJpaEntity;
        QRecordJpaEntity record = QRecordJpaEntity.recordJpaEntity;
        QVoteJpaEntity vote = QVoteJpaEntity.voteJpaEntity;
        QCommentJpaEntity comment = QCommentJpaEntity.commentJpaEntity;
        QPostLikeJpaEntity postLike = QPostLikeJpaEntity.postLikeJpaEntity;

        var query = jpaQueryFactory
                .selectFrom(post)
                .leftJoin(record).on(record.postId.eq(post.postId))
                .leftJoin(vote).on(vote.postId.eq(post.postId))
                .where(
                        filterByRoom(roomId, record, vote),
                        filterByType(type, post, userId),
                        filterByPage(pageStart, pageEnd, record, vote)
                );

        if ("like".equalsIgnoreCase(sort)) {
            query.leftJoin(postLike).on(postLike.postJpaEntity.postId.eq(post.postId));
        } else if ("comment".equalsIgnoreCase(sort)) {
            query.leftJoin(comment).on(comment.postJpaEntity.postId.eq(post.postId));
        }

        query.groupBy(post.postId);

        if ("like".equalsIgnoreCase(sort)) {
            query.orderBy(numberTemplate(Long.class, "count({0})", postLike.likeId).desc());
        } else if ("comment".equalsIgnoreCase(sort)) {
            query.orderBy(numberTemplate(Long.class, "count({0})", comment.commentId).desc());
        } else {
            query.orderBy(post.createdAt.desc());
        }

        int pageSize = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        List<PostJpaEntity> content = query
                .offset(offset)
                .limit(pageSize + 1) // hasNext 판단을 위한 초과 조회
                .fetch();

        boolean hasNext = content.size() > pageSize;
        if (hasNext) {
            content.remove(pageSize);
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private BooleanExpression filterByRoom(Long roomId, QRecordJpaEntity record, QVoteJpaEntity vote) {
        return record.roomJpaEntity.roomId.eq(roomId)
                .or(vote.roomJpaEntity.roomId.eq(roomId));
    }

    private BooleanExpression filterByType(String type, QPostJpaEntity post, Long userId) {
        if ("mine".equalsIgnoreCase(type)) {
            return post.userJpaEntity.userId.eq(userId);
        }
        return null;
    }

    private BooleanExpression filterByPage(Integer start, Integer end, QRecordJpaEntity record, QVoteJpaEntity vote) {
        if (start == null || end == null) {
            return record.isOverview.isTrue().or(vote.isOverview.isTrue());
        }
        return record.page.between(start, end)
                .or(vote.page.between(start, end));
    }
}