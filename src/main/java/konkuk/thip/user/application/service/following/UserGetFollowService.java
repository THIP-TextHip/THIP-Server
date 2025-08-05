package konkuk.thip.user.application.service.following;

import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.user.adapter.in.web.response.UserFollowersResponse;
import konkuk.thip.user.adapter.in.web.response.UserFollowingResponse;
import konkuk.thip.user.application.mapper.FollowQueryMapper;
import konkuk.thip.user.application.port.out.dto.UserQueryDto;
import konkuk.thip.user.application.port.in.UserGetFollowUsecase;
import konkuk.thip.user.application.port.out.FollowingQueryPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserGetFollowService implements UserGetFollowUsecase {

    private final FollowingQueryPort followingQueryPort;
    private final UserCommandPort userCommandPort;

    private final FollowQueryMapper followQueryMapper;

    private static final int MAX_PAGE_SIZE = 10;

    @Override
    @Transactional(readOnly = true)
    public UserFollowersResponse getUserFollowers(Long userId, String cursor, int size) {
        User user = userCommandPort.findById(userId);

        CursorBasedList<UserQueryDto> result = followingQueryPort.getFollowersByUserId(
                user.getId(), cursor, Math.min(size, MAX_PAGE_SIZE)
        );

        var followers = result.contents().stream()
                .map(followQueryMapper::toFollowerDto)
                .toList();

        return UserFollowersResponse.builder()
                .followers(followers)
                .totalFollowerCount(user.getFollowerCount())
                .nextCursor(result.nextCursor())
                .isLast(!result.hasNext())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserFollowingResponse getMyFollowing(Long userId, String cursor, int size) {
        User user = userCommandPort.findById(userId);
        int totalFollowingCount = followingQueryPort.getFollowingCountByUser(user.getId());

        CursorBasedList<UserQueryDto> result = followingQueryPort.getFollowingByUserId(
                user.getId(), cursor, Math.min(size, MAX_PAGE_SIZE)
        );

        var following = result.contents().stream()
                .map(followQueryMapper::toFollowingDto)
                .toList();

        return UserFollowingResponse.builder()
                .followings(following)
                .totalFollowingCount(totalFollowingCount)
                .nextCursor(result.nextCursor())
                .isLast(!result.hasNext())
                .build();
    }
}
