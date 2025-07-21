package konkuk.thip.user.adapter.out.persistence.repository.following;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QAliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QFollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import konkuk.thip.user.application.port.out.dto.FollowQueryDto;
import konkuk.thip.user.application.port.out.dto.QFollowerQueryDto;
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
    public List<FollowQueryDto> findFollowerDtosByUserIdBeforeCreatedAt(Long userId, LocalDateTime cursor, int size) {
        QFollowingJpaEntity following = QFollowingJpaEntity.followingJpaEntity;
        QUserJpaEntity user = QUserJpaEntity.userJpaEntity;
        QAliasJpaEntity alias = QAliasJpaEntity.aliasJpaEntity;

        BooleanBuilder condition = new BooleanBuilder()
                .and(following.followingUserJpaEntity.userId.eq(userId))
                .and(following.status.eq(StatusType.ACTIVE));

        if (cursor != null) {
            condition.and(following.createdAt.lt(cursor));
        }

        return jpaQueryFactory
                .select(new QFollowerQueryDto(
                        user.userId,
                        user.nickname,
                        alias.imageUrl,
                        alias.value,
                        user.followerCount,
                        following.createdAt
                ))
                .from(following)
                .leftJoin(following.userJpaEntity, user)
                .leftJoin(user.aliasForUserJpaEntity, alias)
                .where(condition)
                .orderBy(following.createdAt.desc())
                .limit(size + 1) // hasNext 판단 위해 +1
                .fetch();
    }
}
