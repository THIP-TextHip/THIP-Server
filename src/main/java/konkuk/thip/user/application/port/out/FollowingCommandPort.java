package konkuk.thip.user.application.port.out;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.user.domain.Following;
import konkuk.thip.user.domain.User;

import java.util.Optional;

public interface FollowingCommandPort {

    Optional<Following> findByUserIdAndTargetUserId(Long userId, Long targetUserId);

    default Following getByUserIdAndTargetUserIdOrThrow(Long userId, Long targetUserId) {
        return findByUserIdAndTargetUserId(userId, targetUserId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FOLLOW_NOT_FOUND));
    }

    void save(Following following, User targetUser);

    void deleteFollowing(Following following, User targetUser);

    void deleteAllByUserId(Long userId);

}
