package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;

import java.util.Optional;

public interface FollowingQueryRepository {
    Optional<FollowingJpaEntity> findByUserAndTargetUser(Long userId, Long targetUserId);
}
