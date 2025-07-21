package konkuk.thip.user.application.port.out;

import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.user.application.port.out.dto.FollowerQueryDto;

public interface FollowingQueryPort {
    CursorBasedList<FollowerQueryDto> getFollowersByUserId(Long userId, String cursor, int size);
}

