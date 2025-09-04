package konkuk.thip.user.application.port.out;

import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.user.application.port.out.dto.FollowingQueryDto;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;
import konkuk.thip.user.domain.User;

import java.util.List;

public interface FollowingQueryPort {
    CursorBasedList<UserQueryDto> getFollowersByUserId(Long userId, String cursor, int size);
    CursorBasedList<UserQueryDto> getFollowingByUserId(Long userId, String cursor, int size);

    List<String> getLatestFollowerImageUrls(Long userId, int size);

    boolean isFollowingUser(Long userId, Long targetUserId);

    int getFollowingCountByUser(Long userId);

    /**
     * user가 팔로잉하는 사람들을 조회
     */
    List<FollowingQueryDto> findAllFollowingUsersOrderByFollowedAtDesc(Long userId);

    List<User> getAllFollowersByUserId(Long userId);
}

