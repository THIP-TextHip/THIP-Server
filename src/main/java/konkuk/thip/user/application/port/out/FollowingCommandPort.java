package konkuk.thip.user.application.port.out;

import konkuk.thip.user.domain.Following;

import java.util.Optional;

public interface FollowingCommandPort {

    Optional<Following> findByUserIdAndTargetUserId(Long userId, Long targetUserId);

    void save(Following following);

    void updateStatus(Following following);
}
