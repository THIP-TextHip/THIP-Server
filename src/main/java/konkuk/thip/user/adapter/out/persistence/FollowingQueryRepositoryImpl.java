package konkuk.thip.user.adapter.out.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;
import konkuk.thip.user.adapter.out.jpa.QFollowingJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}
