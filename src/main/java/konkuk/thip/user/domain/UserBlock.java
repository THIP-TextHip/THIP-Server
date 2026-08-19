package konkuk.thip.user.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import static konkuk.thip.common.exception.code.ErrorCode.USER_ALREADY_BLOCKED;
import static konkuk.thip.common.exception.code.ErrorCode.USER_ALREADY_UNBLOCKED;

@Getter
@SuperBuilder
public class UserBlock extends BaseDomainEntity {

    private Long id;

    private Long userId;

    private Long blockedUserId;

    public static UserBlock withoutId(Long userId, Long blockedUserId) {
        return UserBlock.builder()
                .userId(userId)
                .blockedUserId(blockedUserId)
                .status(StatusType.ACTIVE)
                .build();
    }

    public static boolean validateBlockState(boolean isExistingBlock, boolean isBlockRequest) {
        if (isExistingBlock && isBlockRequest) { // 이미 차단한 상태에서 차단 요청을 하는 경우
            throw new InvalidStateException(USER_ALREADY_BLOCKED);
        } else if (!isExistingBlock && !isBlockRequest) { // 차단 해제 요청인데 차단 관계가 존재하지 않는 경우
            throw new InvalidStateException(USER_ALREADY_UNBLOCKED);
        }
        return isBlockRequest;
    }
}
