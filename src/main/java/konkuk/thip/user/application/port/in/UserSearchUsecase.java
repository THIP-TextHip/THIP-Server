package konkuk.thip.user.application.port.in;

import konkuk.thip.user.adapter.in.web.response.UserSearchResponse;
import konkuk.thip.user.application.port.in.dto.UserSearchQuery;

public interface UserSearchUsecase {
    UserSearchResponse searchUsers(UserSearchQuery userSearchQuery);
}
