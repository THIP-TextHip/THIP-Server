package konkuk.thip.user.application.port.out;

import konkuk.thip.user.domain.Following;
import konkuk.thip.user.domain.User;

import java.util.Optional;

public interface FollowingCommandPort {

    Optional<Following> findByUserIdAndTargetUserId(Long userId, Long targetUserId);

    void save(Following following, User user);

    void updateStatus(Following following, User user);
}
