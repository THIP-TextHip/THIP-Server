package konkuk.thip.user.application.port.in;

import konkuk.thip.user.adapter.in.web.response.UserFollowersResponse;

public interface UserGetFollowersUsecase {
    UserFollowersResponse getUserFollowers(Long userId, String nextCursor);
}
