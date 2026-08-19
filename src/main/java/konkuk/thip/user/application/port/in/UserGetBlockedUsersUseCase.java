package konkuk.thip.user.application.port.in;

import konkuk.thip.user.adapter.in.web.response.UserBlockedListResponse;

public interface UserGetBlockedUsersUseCase {

    UserBlockedListResponse getBlockedUsers(Long userId, String cursor, int size);
}
