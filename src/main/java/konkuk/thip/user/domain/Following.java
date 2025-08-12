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

    public static boolean validateFollowingState(boolean isExistingFollowing, boolean isFollowRequest) {
        if (isExistingFollowing && isFollowRequest) { // 이미 팔로우 관계가 존재하는 상태에서 팔로우 요청을 하는 경우
            throw new InvalidStateException(USER_ALREADY_FOLLOWED);
        } else if (!isExistingFollowing && !isFollowRequest) { // 언팔로우 요청을 하는데 팔로우 관계가 존재하지 않는 경우
            throw new InvalidStateException(USER_ALREADY_UNFOLLOWED);
        }
        return isFollowRequest;
    }
}
