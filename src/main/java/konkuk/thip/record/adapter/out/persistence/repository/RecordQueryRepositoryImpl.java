package konkuk.thip.record.adapter.out.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.common.post.PostType;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.post.adapter.out.jpa.QPostJpaEntity;
import konkuk.thip.record.adapter.out.jpa.QRecordJpaEntity;
import konkuk.thip.record.adapter.out.jpa.RecordJpaEntity;
import konkuk.thip.record.adapter.out.persistence.constants.SortType;
import konkuk.thip.record.application.port.out.dto.PostQueryDto;
import konkuk.thip.record.application.port.out.dto.QPostQueryDto;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.QVoteJpaEntity;
import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static konkuk.thip.common.post.PostType.RECORD;
import static konkuk.thip.common.post.PostType.VOTE;


@Repository
@RequiredArgsConstructor
public class RecordQueryRepositoryImpl implements RecordQueryRepository {

    private final JPAQueryFactory queryFactory;

    private final QPostJpaEntity post = QPostJpaEntity.postJpaEntity;
    private final QRecordJpaEntity record = QRecordJpaEntity.recordJpaEntity;
    private final QVoteJpaEntity vote = QVoteJpaEntity.voteJpaEntity;
    private final QUserJpaEntity user = QUserJpaEntity.userJpaEntity;

    @Override
    public List<PostQueryDto> findMyRecords(Long roomId, Long userId, Cursor cursor) {
        BooleanBuilder where = buildMyRecordCondition(roomId, userId);
        SortType sortType = SortType.MINE;

        if (!cursor.isFirstRequest()) {
            where.and(buildCursorPredicateForSortType(sortType, cursor));
        }

        return queryFactory
                .select(selectPostQueryDto())
                .from(post)
                .leftJoin(record).on(post.postId.eq(record.postId))
                .leftJoin(vote).on(post.postId.eq(vote.postId))
                .join(post.userJpaEntity, user)
                .where(where)
                .orderBy(getOrderSpecifiers(sortType))
                .limit(cursor.getPageSize() + 1)
                .fetch();
    }

    private BooleanBuilder buildMyRecordCondition(Long roomId, Long userId) {
        BooleanBuilder where = new BooleanBuilder();

        BooleanBuilder voteCondition = new BooleanBuilder();
        voteCondition.and(post.instanceOf(VoteJpaEntity.class))
                .and(vote.roomJpaEntity.roomId.eq(roomId));

        BooleanBuilder recordCondition = new BooleanBuilder();
        recordCondition.and(post.instanceOf(RecordJpaEntity.class))
                .and(record.roomJpaEntity.roomId.eq(roomId));

        where.and(voteCondition.or(recordCondition))
                .and(post.userJpaEntity.userId.eq(userId))
                .and(post.status.eq(StatusType.ACTIVE));
        return where;
    }

    @Override
    public List<PostQueryDto> findGroupRecordsOrderBySortType(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview, SortType sortType) {
        BooleanBuilder where = buildRecordVoteCondition(roomId, pageStart, pageEnd, isOverview);

        if (!cursor.isFirstRequest()) {
            where.and(buildCursorPredicateForSortType(sortType, cursor));
        }

        return queryFactory
                .select(selectPostQueryDto())
                .from(post)
                .leftJoin(record).on(post.postId.eq(record.postId))
                .leftJoin(vote).on(post.postId.eq(vote.postId))
                .join(post.userJpaEntity, user)
                .where(where)
                .orderBy(getOrderSpecifiers(sortType))
                .limit(cursor.getPageSize() + 1)
                .fetch();
    }

    private BooleanBuilder buildRecordVoteCondition(Long roomId, Integer pageStart, Integer pageEnd, Boolean isOverview) {
        BooleanBuilder where = new BooleanBuilder();

        BooleanBuilder voteCondition = new BooleanBuilder();
        voteCondition.and(post.instanceOf(VoteJpaEntity.class))
                .and(vote.roomJpaEntity.roomId.eq(roomId));

        if (isOverview) {
            voteCondition.and(vote.isOverview.isTrue());
        } else {
            voteCondition.and(vote.isOverview.isFalse())
                    .and(vote.page.between(pageStart, pageEnd));
        }

        BooleanBuilder recordCondition = new BooleanBuilder();
        recordCondition.and(post.instanceOf(RecordJpaEntity.class))
                .and(record.roomJpaEntity.roomId.eq(roomId));

        if (isOverview) {
            recordCondition.and(record.isOverview.isTrue());
        } else {
            recordCondition.and(record.isOverview.isFalse())
                    .and(record.page.between(pageStart, pageEnd));
        }

        where.and(voteCondition.or(recordCondition))
                .and(post.status.eq(StatusType.ACTIVE));
        return where;
    }

    // Case: pageExpr (Record, Vote 분기)
    private NumberExpression<Integer> pageExpr() {
        return new CaseBuilder()
                .when(post.instanceOf(RecordJpaEntity.class)).then(record.page)
                .when(post.instanceOf(VoteJpaEntity.class)).then(vote.page)
                .otherwise(0);
    }

    // Case: isOverviewExpr (총평 여부를 정렬 기준으로 사용)
    private NumberExpression<Integer> isOverviewExpr() {
        return new CaseBuilder()
                .when(post.instanceOf(RecordJpaEntity.class)).then(record.isOverview.castToNum(Integer.class))
                .when(post.instanceOf(VoteJpaEntity.class)).then(vote.isOverview.castToNum(Integer.class))
                .otherwise(0);
    }

    // Case: postTypeExpr ("RECORD" or "VOTE")
    private StringExpression postTypeExpr() {
        return new CaseBuilder()
                .when(post.instanceOf(RecordJpaEntity.class)).then(RECORD.getType())
                .when(post.instanceOf(VoteJpaEntity.class)).then(VOTE.getType())
                .otherwise(PostType.from("UNKNOWN").getType()); // 타입 불일치 예외 발생
    }

    private BooleanBuilder buildCursorPredicateForSortType(SortType sortType, Cursor cursor) {
        BooleanBuilder builder = new BooleanBuilder();

        switch (sortType) {
            case CREATED_AT -> {
                LocalDateTime createdAt = cursor.getLocalDateTime(0);
                Long postId = cursor.getLong(1);
                builder.and(post.createdAt.lt(createdAt)
                        .or(post.createdAt.eq(createdAt).and(post.postId.lt(postId))));
            }
            case LIKE_COUNT -> {
                Integer likeCount = cursor.getInteger(0);
                Long postId = cursor.getLong(1);
                builder.and(post.likeCount.lt(likeCount)
                        .or(post.likeCount.eq(likeCount).and(post.postId.lt(postId))));
            }
            case COMMENT_COUNT -> {
                Integer commentCount = cursor.getInteger(0);
                Long postId = cursor.getLong(1);
                builder.and(post.commentCount.lt(commentCount)
                        .or(post.commentCount.eq(commentCount).and(post.postId.lt(postId))));
            }
            case MINE -> {
                Integer isOverview = cursor.getInteger(0);
                Integer page = cursor.getInteger(1);
                Long postId = cursor.getLong(2);
                builder.and(
                        isOverviewExpr().lt(isOverview)
                                .or(isOverviewExpr().eq(isOverview).and(pageExpr().lt(page)))
                                .or(isOverviewExpr().eq(isOverview).and(pageExpr().eq(page)).and(post.postId.lt(postId)))
                );
            }
        }

        return builder;
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(SortType sortType) {
        return switch (sortType) {
            case CREATED_AT -> new OrderSpecifier[] { post.createdAt.desc(), post.postId.desc() };
            case LIKE_COUNT -> new OrderSpecifier[] { post.likeCount.desc(), post.postId.desc() };
            case COMMENT_COUNT -> new OrderSpecifier[] { post.commentCount.desc(), post.postId.desc() };
            case MINE -> new OrderSpecifier[] { isOverviewExpr().desc(), pageExpr().desc(), post.postId.desc() };
        };
    }

    private QPostQueryDto selectPostQueryDto() {
        return new QPostQueryDto(
                post.postId,
                postTypeExpr(), //추후에 상속 구조 해지시 type 필드로 구분
                post.createdAt,
                pageExpr(),
                user.userId,
                user.nickname,
                user.imageUrl,
                post.content,
                post.likeCount,
                post.commentCount,
                isOverviewExpr().eq(1)
        );
    }
}