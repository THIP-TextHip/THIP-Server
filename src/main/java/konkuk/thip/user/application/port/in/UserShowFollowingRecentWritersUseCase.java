package konkuk.thip.user.application.port.in;

import konkuk.thip.user.adapter.in.web.response.UserFollowingRecentWritersResponse;

public interface UserShowFollowingRecentWritersUseCase {

    UserFollowingRecentWritersResponse showMyFollowingRecentWriters(Long userId);
}
