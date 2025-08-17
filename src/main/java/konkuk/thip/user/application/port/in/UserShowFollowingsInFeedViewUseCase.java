package konkuk.thip.user.application.port.in;

import konkuk.thip.user.adapter.in.web.response.UserShowFollowingsInFeedViewResponse;

public interface UserShowFollowingsInFeedViewUseCase {

    UserShowFollowingsInFeedViewResponse showMyFollowingsInFeedView(Long userId);
}
