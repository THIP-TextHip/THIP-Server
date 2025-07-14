package konkuk.thip.user.application.port.out;

import konkuk.thip.user.adapter.in.web.response.UserFollowersResponse;

import java.util.List;
import java.util.Map;

public interface FollowingQueryPort {
    Map<Long, Integer> countByFollowingUserIds(List<Long> userIds);

    UserFollowersResponse getFollowersByUserId(Long userId, String cursor, int size);
}

