package konkuk.thip.user.application.service;

import konkuk.thip.user.adapter.in.web.response.UserFollowersResponse;
import konkuk.thip.user.application.port.in.UserGetFollowersUsecase;
import konkuk.thip.user.application.port.out.FollowingQueryPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserGetFollowersService implements UserGetFollowersUsecase {

    private final FollowingQueryPort followingQueryPort;
    private final UserCommandPort userCommandPort;

    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    @Transactional(readOnly = true)
    public UserFollowersResponse getUserFollowers(Long userId, String cursor) {
        User user = userCommandPort.findById(userId);
        return followingQueryPort.getFollowersByUserId(user.getId(), cursor, DEFAULT_PAGE_SIZE);
    }
}
