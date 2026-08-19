package konkuk.thip.user.application.port.out;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.user.domain.UserBlock;

import java.util.Optional;

public interface UserBlockCommandPort {

    Optional<UserBlock> findByUserIdAndTargetUserId(Long userId, Long targetUserId);

    default UserBlock getByUserIdAndTargetUserIdOrThrow(Long userId, Long targetUserId) {
        return findByUserIdAndTargetUserId(userId, targetUserId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BLOCK_NOT_FOUND));
    }

    void save(UserBlock userBlock);

    void deleteBlock(UserBlock userBlock);

    void deleteAllByUserId(Long userId);
}
