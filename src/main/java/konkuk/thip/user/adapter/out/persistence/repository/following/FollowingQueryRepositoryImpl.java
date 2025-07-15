package konkuk.thip.user.adapter.out.persistence.repository.following;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QAliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QFollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QUserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FollowingQueryRepositoryImpl implements FollowingQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    // 주어진 userId 리스트에 대해 각 userId의 팔로워(구독자) 수를 집계하여 Map으로 반환
    public Map<Long, Integer> countByFollowingUserIds(List<Long> userIds) {

        QFollowingJpaEntity following = QFollowingJpaEntity.followingJpaEntity;

        List<Tuple> results = jpaQueryFactory
                .select(following.followingUserJpaEntity.userId, following.count())
                .from(following)
                .where(following.followingUserJpaEntity.userId.in(userIds))
                .groupBy(following.followingUserJpaEntity.userId)
                .fetch();

        // 결과를 Map<Long, Integer>로 변환
        return results.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(following.followingUserJpaEntity.userId),
                        tuple -> tuple.get(following.count()).intValue()
                ));
    }

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
    public List<FollowingJpaEntity> findFollowersByUserIdBeforeCreatedAt(Long userId, LocalDateTime cursor, int size) {
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
                .selectFrom(following)
                .leftJoin(following.userJpaEntity, user).fetchJoin() // N+1 문제 방지를 위해 fetchJoin
                .leftJoin(user.aliasForUserJpaEntity, alias).fetchJoin()
                .where(condition)
                .orderBy(following.createdAt.desc())
                .limit(size)
                .fetch();
    }
}
