package konkuk.thip.user.application.port.out;

import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.user.application.port.out.dto.FollowQueryDto;

public interface FollowingQueryPort {
    CursorBasedList<FollowQueryDto> getFollowersByUserId(Long userId, String cursor, int size);
    CursorBasedList<FollowQueryDto> getFollowingByUserId(Long userId, String cursor, int size);
}

