package konkuk.thip.user.application.port.in;

import konkuk.thip.user.adapter.in.web.response.UserIsFollowingRespone;

public interface UserIsFollowingUsecase {
    UserIsFollowingRespone isFollowing(Long userId, Long targetUserId);
}
