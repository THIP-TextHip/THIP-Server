package konkuk.thip.user.application.port.in;

import konkuk.thip.user.adapter.in.web.response.UserFollowersResponse;
import konkuk.thip.user.adapter.in.web.response.UserFollowingResponse;

public interface UserGetFollowUsecase {
    UserFollowersResponse getUserFollowers(Long LoginUserId, Long userId, String cursor, int size);

    UserFollowingResponse getMyFollowing(Long userId, String cursor, int size);
}
