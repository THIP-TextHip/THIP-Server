package konkuk.thip.user.application.port.out;

import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;

import java.util.List;

public interface FollowingQueryPort {
    CursorBasedList<UserQueryDto> getFollowersByUserId(Long userId, String cursor, int size);
    CursorBasedList<UserQueryDto> getFollowingByUserId(Long userId, String cursor, int size);

    List<String> getLatestFollowerImageUrls(Long userId, int size);

    boolean isFollowingUser(Long userId, Long targetUserId);

    int getFollowingCountByUser(Long userId);
}

