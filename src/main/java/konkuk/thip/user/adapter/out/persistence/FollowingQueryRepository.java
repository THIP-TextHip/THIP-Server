package konkuk.thip.user.adapter.out.persistence;

import konkuk.thip.user.adapter.out.jpa.FollowingJpaEntity;

import java.util.Optional;
import java.util.List;
import java.util.Map;

public interface FollowingQueryRepository {
    Map<Long, Integer> countByFollowingUserIds(List<Long> userIds);
    Optional<FollowingJpaEntity> findByUserAndTargetUser(Long userId, Long targetUserId);
}
