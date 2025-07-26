package konkuk.thip.user.adapter.out.persistence.repository.following;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QAliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QFollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import konkuk.thip.user.application.port.out.dto.FollowQueryDto;
import konkuk.thip.user.application.port.out.dto.QFollowQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FollowingQueryRepositoryImpl implements FollowingQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<FollowingJpaEntity> findByUserAndTargetUser(Long userId, Long targetUserId) {
        QFollowingJpaEntity following = QFollowingJpaEntity.followingJpaEntity;

        FollowingJpaEntity followingJpaEntity = jpaQueryFactory
                .selectFrom(following)
                .where(following.userJpaEntity.userId.eq(userId)
                        .and(following.followingUserJpaEntity.userId.eq(targetUserId))
                        .and(following.status.eq(StatusType.ACTIVE)))
                .fetchOne();

        return Optional.ofNullable(followingJpaEntity);
    }

    @Override
    public List<FollowQueryDto> findFollowerDtosByUserIdBeforeCreatedAt(Long userId, LocalDateTime cursor, int size) {
        return findFollowDtos(
                userId,
                cursor,
                size,
                true // isFollowerQuery
        );
    }

    @Override
    public List<FollowQueryDto> findFollowingDtosByUserIdBeforeCreatedAt(Long userId, LocalDateTime cursor, int size) {
        return findFollowDtos(
                userId,
                cursor,
                size,
                false // isFollowingQuery
        );
    }

    private List<FollowQueryDto> findFollowDtos(Long userId, LocalDateTime cursor, int size, boolean isFollowerQuery) {
        QFollowingJpaEntity following = QFollowingJpaEntity.followingJpaEntity;
        QUserJpaEntity user = QUserJpaEntity.userJpaEntity;
        QAliasJpaEntity alias = QAliasJpaEntity.aliasJpaEntity;

        BooleanBuilder condition = new BooleanBuilder()
                .and((isFollowerQuery ? following.followingUserJpaEntity.userId.eq(userId) : following.userJpaEntity.userId.eq(userId)))
                .and(following.status.eq(StatusType.ACTIVE));

        if (cursor != null) {
            condition.and(following.createdAt.lt(cursor));
        }

        QUserJpaEntity targetUser = isFollowerQuery ? following.userJpaEntity : following.followingUserJpaEntity;

        return jpaQueryFactory
                .select(new QFollowQueryDto(
                        targetUser.userId,
                        targetUser.nickname,
                        alias.imageUrl,
                        alias.value,
                        targetUser.followerCount,
                        following.createdAt
                ))
                .from(following)
                .leftJoin(targetUser, user)
                .leftJoin(user.aliasForUserJpaEntity, alias)
                .where(condition)
                .orderBy(following.createdAt.desc())
                .limit(size + 1)
                .fetch();
    }
}
