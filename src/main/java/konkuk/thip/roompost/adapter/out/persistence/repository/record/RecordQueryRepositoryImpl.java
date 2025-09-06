package konkuk.thip.roompost.adapter.out.persistence.repository.record;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.post.adapter.out.jpa.QPostJpaEntity;
import konkuk.thip.post.application.port.out.dto.PostQueryDto;
import konkuk.thip.post.application.port.out.dto.QPostQueryDto;
import konkuk.thip.roompost.adapter.out.jpa.QRecordJpaEntity;
import konkuk.thip.roompost.adapter.out.jpa.QVoteJpaEntity;
import konkuk.thip.roompost.adapter.out.persistence.RoomPostSortType;
import konkuk.thip.roompost.application.port.out.dto.QRoomPostQueryDto;
import konkuk.thip.roompost.application.port.out.dto.RoomPostQueryDto;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.querydsl.jpa.JPAExpressions.treat;
import static konkuk.thip.post.domain.PostType.RECORD;
import static konkuk.thip.post.domain.PostType.VOTE;

@Repository
@RequiredArgsConstructor
public class RecordQueryRepositoryImpl implements RecordQueryRepository {

    private final JPAQueryFactory queryFactory;

    private final QPostJpaEntity post = QPostJpaEntity.postJpaEntity;
    private final QUserJpaEntity user = QUserJpaEntity.userJpaEntity;

    @Override
    public List<RoomPostQueryDto> findMyRecords(Long roomId, Long userId, Cursor cursor) {
        BooleanBuilder where = buildMyRecordCondition(roomId, userId);
        RoomPostSortType roomPostSortType = RoomPostSortType.MINE;

        if (!cursor.isFirstRequest()) {
            where.and(buildCursorPredicateForSortType(roomPostSortType, cursor));
        }

        return queryFactory
                .select(selectPostQueryDto())
                .from(post)
                .join(post.userJpaEntity, user)
                .where(where)
                .orderBy(getOrderSpecifiers(roomPostSortType))
                .limit(cursor.getPageSize() + 1)
                .fetch();
    }

    private BooleanBuilder buildMyRecordCondition(Long roomId, Long userId) {
        BooleanBuilder where = new BooleanBuilder();

        BooleanBuilder voteCondition = new BooleanBuilder()
                .and(post.dtype.eq(VOTE.getType()))
                .and(treat(post, QVoteJpaEntity.class).roomJpaEntity.roomId.eq(roomId));

        BooleanBuilder recordCondition = new BooleanBuilder()
                .and(post.dtype.eq(RECORD.getType()))
                .and(treat(post, QRecordJpaEntity.class).roomJpaEntity.roomId.eq(roomId));

        where.and(voteCondition.or(recordCondition))
                .and(post.userJpaEntity.userId.eq(userId));

        return where;
    }

    @Override
    public List<RoomPostQueryDto> findGroupRecordsOrderBySortType(Long roomId, Long userId, Cursor cursor, Integer pageStart, Integer pageEnd, Boolean isOverview, RoomPostSortType roomPostSortType) {
        BooleanBuilder where = buildRecordVoteCondition(roomId, pageStart, pageEnd, isOverview);

        if (!cursor.isFirstRequest()) {
            where.and(buildCursorPredicateForSortType(roomPostSortType, cursor));
        }

        return queryFactory
                .select(selectPostQueryDto())
                .from(post)
                .join(post.userJpaEntity, user)
                .where(where)
                .orderBy(getOrderSpecifiers(roomPostSortType))
                .limit(cursor.getPageSize() + 1)
                .fetch();
    }

    private BooleanBuilder buildRecordVoteCondition(Long roomId, Integer pageStart, Integer pageEnd, Boolean isOverview) {
        BooleanBuilder where = new BooleanBuilder();

        // VOTE
        BooleanBuilder voteCondition = new BooleanBuilder()
                .and(post.dtype.eq(VOTE.getType()))
                .and(treat(post, QVoteJpaEntity.class).roomJpaEntity.roomId.eq(roomId));

        if (isOverview) {
            voteCondition.and(treat(post, QVoteJpaEntity.class).isOverview.isTrue());
        } else {
            voteCondition.and(treat(post, QVoteJpaEntity.class).isOverview.isFalse())
                    .and(treat(post, QVoteJpaEntity.class).page.between(pageStart, pageEnd));
        }

        // RECORD
        BooleanBuilder recordCondition = new BooleanBuilder()
                .and(post.dtype.eq(RECORD.getType()))
                .and(treat(post, QRecordJpaEntity.class).roomJpaEntity.roomId.eq(roomId));

        if (isOverview) {
            recordCondition.and(treat(post, QRecordJpaEntity.class).isOverview.isTrue());
        } else {
            recordCondition.and(treat(post, QRecordJpaEntity.class).isOverview.isFalse())
                    .and(treat(post, QRecordJpaEntity.class).page.between(pageStart, pageEnd));
        }

        where.and(voteCondition.or(recordCondition));

        return where;
    }

    // Case: pageExpr (Record, Vote 분기)
    private NumberExpression<Integer> pageExpr() {
        return new CaseBuilder()
                .when(post.dtype.eq(RECORD.getType()))
                .then(treat(post, QRecordJpaEntity.class).page)
                .when(post.dtype.eq(VOTE.getType()))
                .then(treat(post, QVoteJpaEntity.class).page)
                .otherwise(0);
    }

    // Case: isOverviewExpr (총평 여부를 정렬 기준으로 사용)
    private NumberExpression<Integer> isOverviewExpr() {
        return new CaseBuilder()
                .when(post.dtype.eq(RECORD.getType()))
                .then(treat(post, QRecordJpaEntity.class).isOverview.castToNum(Integer.class))
                .when(post.dtype.eq(VOTE.getType()))
                .then(treat(post, QVoteJpaEntity.class).isOverview.castToNum(Integer.class))
                .otherwise(0);
    }

    private BooleanBuilder buildCursorPredicateForSortType(RoomPostSortType roomPostSortType, Cursor cursor) {
        BooleanBuilder builder = new BooleanBuilder();

        switch (roomPostSortType) {
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

    private OrderSpecifier<?>[] getOrderSpecifiers(RoomPostSortType roomPostSortType) {
        return switch (roomPostSortType) {
            case CREATED_AT -> new OrderSpecifier[] { post.createdAt.desc(), post.postId.desc() };
            case LIKE_COUNT -> new OrderSpecifier[] { post.likeCount.desc(), post.postId.desc() };
            case COMMENT_COUNT -> new OrderSpecifier[] { post.commentCount.desc(), post.postId.desc() };
            case MINE -> new OrderSpecifier[] { isOverviewExpr().desc(), pageExpr().desc(), post.postId.desc() };
        };
    }

    private QRoomPostQueryDto selectPostQueryDto() {
        return new QRoomPostQueryDto(
                post.postId,
                post.dtype, //추후에 상속 구조 해지시 type 필드로 구분
                post.createdAt,
                pageExpr(),
                user.userId,
                user.nickname,
                user.alias,
                post.content,
                post.likeCount,
                post.commentCount,
                isOverviewExpr().eq(1)
        );
    }

    @Override
    public PostQueryDto getPostQueryDtoByPostId(Long postId) {
        return queryFactory
                .select(new QPostQueryDto(
                        post.postId,
                        post.userJpaEntity.userId,
                        post.dtype,
                        pageExprForDto(),    // dtype에 따라 Record/Vote의 page
                        roomIdExprForDto()   // dtype에 따라 Record/Vote의 roomId
                ))
                .from(post)
                .where(post.postId.eq(postId))
                .fetchOne();
    }

    private NumberExpression<Integer> pageExprForDto() {
        return new CaseBuilder()
                .when(post.dtype.eq(RECORD.getType()))
                .then(treat(post, QRecordJpaEntity.class).page)
                .when(post.dtype.eq(VOTE.getType()))
                .then(treat(post, QVoteJpaEntity.class).page)
                .otherwise((Integer) null);
    }

    private NumberExpression<Long> roomIdExprForDto() {
        return new CaseBuilder()
                .when(post.dtype.eq(RECORD.getType()))
                .then(treat(post, QRecordJpaEntity.class).roomJpaEntity.roomId)
                .when(post.dtype.eq(VOTE.getType()))
                .then(treat(post, QVoteJpaEntity.class).roomJpaEntity.roomId)
                .otherwise((Long) null);
    }
}
