package konkuk.thip.user.application.port.in;

import konkuk.thip.user.adapter.in.web.response.UserFollowersResponse;

public interface UserGetFollowUsecase {
    UserFollowersResponse getUserFollowers(Long userId, String cursor);

    UserFollowersResponse getMyFollowing(String cursor);
}
