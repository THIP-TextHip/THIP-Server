package konkuk.thip.user.adapter.out.persistence.repository.following;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QAliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QFollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import konkuk.thip.user.application.port.out.dto.QUserQueryDto;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;
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
    public List<UserQueryDto> findFollowerDtosByUserIdBeforeCreatedAt(Long userId, LocalDateTime cursor, int size) {
        return findFollowDtos(
                userId,
                cursor,
                size,
                true // isFollowerQuery
        );
    }

    @Override
    public List<UserQueryDto> findFollowingDtosByUserIdBeforeCreatedAt(Long userId, LocalDateTime cursor, int size) {
        return findFollowDtos(
                userId,
                cursor,
                size,
                false // isFollowingQuery
        );
    }

    private List<UserQueryDto> findFollowDtos(Long userId, LocalDateTime cursor, int size, boolean isFollowerQuery) {
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
                .select(new QUserQueryDto(
                        targetUser.userId,
                        targetUser.nickname,
                        alias.imageUrl,
                        alias.value,
                        alias.color,
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

    @Override
    public List<String> findLatestFollowerImageUrls(Long userId, int size) {
        QFollowingJpaEntity following = QFollowingJpaEntity.followingJpaEntity;
        QUserJpaEntity follower = QUserJpaEntity.userJpaEntity;     // userId 를 팔로우하는 사람들(= follower)
        QAliasJpaEntity alias = QAliasJpaEntity.aliasJpaEntity;

        return jpaQueryFactory
                .select(alias.imageUrl)
                .from(following)
                .join(following.userJpaEntity, follower)
                .join(follower.aliasForUserJpaEntity, alias)
                .where(following.followingUserJpaEntity.userId.eq(userId)
                        .and(following.status.eq(StatusType.ACTIVE)))
                .orderBy(following.createdAt.desc())
                .limit(size)
                .fetch();
    }
}
