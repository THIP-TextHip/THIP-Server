package konkuk.thip.user.adapter.out.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.comment.adapter.out.jpa.QCommentJpaEntity;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.feed.adapter.out.jpa.QFeedJpaEntity;
import konkuk.thip.post.adapter.out.jpa.QPostJpaEntity;
import konkuk.thip.post.adapter.out.jpa.QPostLikeJpaEntity;
import konkuk.thip.room.adapter.out.jpa.QRoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.QRoomParticipantJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QAliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QFollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import konkuk.thip.user.application.port.out.dto.QReactionQueryDto;
import konkuk.thip.user.application.port.out.dto.QUserQueryDto;
import konkuk.thip.user.application.port.out.dto.ReactionQueryDto;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Set<Long> findUserIdsByBookId(Long bookId) {
        QRoomParticipantJpaEntity userRoom = QRoomParticipantJpaEntity.roomParticipantJpaEntity;
        QRoomJpaEntity room = QRoomJpaEntity.roomJpaEntity;

        return new HashSet<>(
                queryFactory
                        .select(userRoom.userJpaEntity.userId)
                        .distinct()
                        .from(userRoom)
                        .join(userRoom.roomJpaEntity, room)
                        .where(room.bookJpaEntity.bookId.eq(bookId))
                        .fetch()
        );
    }

    @Override
    public List<UserQueryDto> findUsersByNicknameOrderByAccuracy(String keyword, Long userId, Integer size) {
        QUserJpaEntity user = QUserJpaEntity.userJpaEntity;
        QAliasJpaEntity alias = QAliasJpaEntity.aliasJpaEntity;

        String pattern = "%" + keyword + "%";

        NumberExpression<Integer> priority = new CaseBuilder()
                .when(user.nickname.eq(keyword)).then(3)
                .when(user.nickname.like(keyword + "%")).then(2)
                .when(user.nickname.like(pattern)).then(1)
                .otherwise(0);

        return queryFactory
                .select(new QUserQueryDto(
                        user.userId,
                        user.nickname,
                        alias.imageUrl,
                        alias.value,
                        alias.color,
                        user.followerCount,
                        user.createdAt
                ))
                .from(user)
                .leftJoin(user.aliasForUserJpaEntity, alias)
                .where(user.nickname.like(pattern)
                        .and(user.userId.ne(userId))
                        .and(user.status.eq(StatusType.ACTIVE)))
                .orderBy(priority.desc(), user.nickname.asc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<ReactionQueryDto> findLikeByUserId(Long userId, LocalDateTime cursorLocalDateTime, Integer size, String likeLabel) {
        QUserJpaEntity user = QUserJpaEntity.userJpaEntity;
        QPostLikeJpaEntity postLike = QPostLikeJpaEntity.postLikeJpaEntity;
        QPostJpaEntity post = QPostJpaEntity.postJpaEntity;

        BooleanBuilder where = new BooleanBuilder();
        where.and(user.userId.eq(userId))
                .and(post.status.eq(StatusType.ACTIVE))
                .and(postLike.status.eq(StatusType.ACTIVE));
        if (cursorLocalDateTime != null) {
            where.and(postLike.createdAt.lt(cursorLocalDateTime));
        }

        return queryFactory
                .select(new QReactionQueryDto(
                        Expressions.constant(likeLabel),
                        post.postId,
                        post.userJpaEntity.nickname,
                        post.userJpaEntity.userId,
                        post.dtype,
                        post.content,
                        postLike.createdAt
                ))
                .from(postLike)
                .join(postLike.postJpaEntity, post)
                .join(postLike.userJpaEntity, user)
                .where(where)
                .orderBy(postLike.createdAt.desc())
                .limit(size + 1)
                .fetch();
    }

    @Override
    public List<ReactionQueryDto> findCommentByUserId(Long userId, LocalDateTime cursorLocalDateTime, Integer size, String commentLabel) {
        QUserJpaEntity user = QUserJpaEntity.userJpaEntity;
        QPostJpaEntity post = QPostJpaEntity.postJpaEntity;
        QCommentJpaEntity comment = QCommentJpaEntity.commentJpaEntity;

        BooleanBuilder where = new BooleanBuilder();
        where.and(user.userId.eq(userId))
                .and(post.status.eq(StatusType.ACTIVE))
                .and(comment.status.eq(StatusType.ACTIVE));
        if (cursorLocalDateTime != null) {
            where.and(comment.createdAt.lt(cursorLocalDateTime));
        }

        return queryFactory
                .select(new QReactionQueryDto(
                        Expressions.constant(commentLabel),
                        post.postId,
                        post.userJpaEntity.nickname,
                        post.userJpaEntity.userId,
                        post.dtype,
                        comment.content,
                        comment.createdAt
                ))
                .from(comment)
                .join(comment.userJpaEntity, user)
                .join(comment.postJpaEntity, post)
                .where(where)
                .orderBy(comment.createdAt.desc())
                .limit(size + 1)
                .fetch();
    }

    @Override
    public List<ReactionQueryDto> findLikeAndCommentByUserId(Long userId, LocalDateTime cursor, Integer size, String likeLabel, String commentLabel) {
        List<ReactionQueryDto> likes = findLikeByUserId(userId, cursor, size, likeLabel);
        List<ReactionQueryDto> comments = findCommentByUserId(userId, cursor, size, commentLabel);

        return Stream.concat(likes.stream(), comments.stream())
                .sorted(Comparator.comparing(ReactionQueryDto::createdAt).reversed())
                .limit(size + 1)
                .toList();
    }
}
