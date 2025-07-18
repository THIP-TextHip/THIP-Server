package konkuk.thip.user.adapter.out.persistence.repository.following;

import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FollowingQueryRepository {
    Optional<FollowingJpaEntity> findByUserAndTargetUser(Long userId, Long targetUserId);

    List<FollowingJpaEntity> findFollowersByUserIdBeforeCreatedAt(Long userId, LocalDateTime cursor, int size);
}
