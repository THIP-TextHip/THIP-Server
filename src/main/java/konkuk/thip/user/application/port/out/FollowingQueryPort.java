package konkuk.thip.user.application.port.out;

import konkuk.thip.user.adapter.in.web.response.UserFollowersResponse;

public interface FollowingQueryPort {
    UserFollowersResponse getFollowersByUserId(Long userId, String cursor, int size);
}

