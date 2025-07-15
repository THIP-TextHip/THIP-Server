package konkuk.thip.user.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.entity.StatusType;
import konkuk.thip.common.exception.InvalidStateException;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import static konkuk.thip.common.exception.code.ErrorCode.USER_ALREADY_FOLLOWED;
import static konkuk.thip.common.exception.code.ErrorCode.USER_ALREADY_UNFOLLOWED;

@Getter
@SuperBuilder
public class Following extends BaseDomainEntity {

    private Long id;

    private Long userId;

    private Long followingUserId;

    public static Following withoutId(Long userId, Long followingUserId) {
        return Following.builder()
                .userId(userId)
                .followingUserId(followingUserId)
                .status(StatusType.ACTIVE)
                .build();
    }

    public boolean changeFollowingState(boolean isFollowRequest) {
        StatusType currentStatus = getStatus();
        validateFollowingState(isFollowRequest, currentStatus);

        super.changeStatus();
        return isFollowRequest;
    }

    private void validateFollowingState(boolean isFollowRequest, StatusType currentStatus) {
        if (isFollowRequest && currentStatus == StatusType.ACTIVE) { // 팔로우 요청일 때 이미 팔로우 중인 경우
            throw new InvalidStateException(USER_ALREADY_FOLLOWED);
        }

        if (!isFollowRequest && currentStatus == StatusType.INACTIVE) { // 언팔로우 요청일 때 이미 언팔로우 중인 경우
            throw new InvalidStateException(USER_ALREADY_UNFOLLOWED);
        }
    }
}
