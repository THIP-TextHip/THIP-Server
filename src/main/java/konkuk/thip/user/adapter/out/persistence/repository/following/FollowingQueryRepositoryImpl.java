package konkuk.thip.user.adapter.out.persistence.repository.following;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QFollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.application.port.out.dto.FollowingQueryDto;
import konkuk.thip.user.application.port.out.dto.QFollowingQueryDto;
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
                        .and(following.followingUserJpaEntity.userId.eq(targetUserId)))
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

        BooleanBuilder condition = new BooleanBuilder()
                .and((isFollowerQuery
                        ? following.followingUserJpaEntity.userId.eq(userId)    // 나를 팔로우한 사람들
                        : following.userJpaEntity.userId.eq(userId)));  // 내가 팔로우하는 사람들

        if (cursor != null) {
            condition.and(following.createdAt.lt(cursor));
        }

        QUserJpaEntity targetUser = isFollowerQuery ? following.userJpaEntity : following.followingUserJpaEntity;

        return jpaQueryFactory
                .select(new QUserQueryDto(
                        targetUser.userId,
                        targetUser.nickname,
                        targetUser.alias,
                        targetUser.followerCount,
                        following.createdAt
                ))
                .from(following)
                .join(targetUser, user)
                .where(condition)
                .orderBy(following.createdAt.desc())
                .limit(size + 1)
                .fetch();
    }

    @Override
    public List<UserJpaEntity> findLatestFollowers(Long userId, int size) {
        QFollowingJpaEntity following = QFollowingJpaEntity.followingJpaEntity;
        QUserJpaEntity follower = QUserJpaEntity.userJpaEntity;     // userId 를 팔로우하는 사람들(= follower)

        return jpaQueryFactory
                .select(follower)
                .from(following)
                .join(following.userJpaEntity, follower)
                .where(following.followingUserJpaEntity.userId.eq(userId))
                .orderBy(following.createdAt.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<FollowingQueryDto> findAllFollowingUsersOrderByFollowedAtDesc(Long userId) {
        QFollowingJpaEntity following = QFollowingJpaEntity.followingJpaEntity;
        QUserJpaEntity followingTargetUser = QUserJpaEntity.userJpaEntity;

        return jpaQueryFactory.select(new QFollowingQueryDto(
                following.userJpaEntity.userId,
                followingTargetUser.userId,
                followingTargetUser.nickname,
                followingTargetUser.alias,
                following.createdAt
                ))
                .from(following)
                .join(following.followingUserJpaEntity, followingTargetUser)
                .where(following.userJpaEntity.userId.eq(userId))
                .orderBy(following.createdAt.desc())
                .fetch();
    }
}
