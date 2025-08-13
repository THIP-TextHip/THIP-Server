package konkuk.thip.user.application.service;

import konkuk.thip.user.adapter.in.web.response.UserFollowingRecentWritersResponse;
import konkuk.thip.user.application.mapper.UserQueryMapper;
import konkuk.thip.user.application.port.in.UserShowFollowingRecentWritersUseCase;
import konkuk.thip.user.application.port.out.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserShowFollowingRecentWritersService implements UserShowFollowingRecentWritersUseCase {

    private static final int SIZE = 10;
    private final UserQueryPort userQueryPort;
    private final UserQueryMapper userQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public UserFollowingRecentWritersResponse showMyFollowingRecentWriters(Long userId) {
        return userQueryMapper.toRecentWriterResponses(userQueryPort.findRecentFeedWritersOfMyFollowings(userId, SIZE));
    }
}
