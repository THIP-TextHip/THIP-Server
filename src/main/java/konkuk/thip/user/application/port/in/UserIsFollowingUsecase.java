package konkuk.thip.user.application.port.in;

import konkuk.thip.user.adapter.in.web.response.UserIsFollowingResponse;

public interface UserIsFollowingUsecase {
    UserIsFollowingResponse isFollowing(Long userId, Long targetUserId);
}
