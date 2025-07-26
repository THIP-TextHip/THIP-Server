package konkuk.thip.user.application.service;

import konkuk.thip.user.adapter.in.web.response.UserIsFollowingResponse;
import konkuk.thip.user.application.port.in.UserIsFollowingUsecase;
import konkuk.thip.user.application.port.out.FollowingCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserIsFollowingService implements UserIsFollowingUsecase {

    private final FollowingCommandPort followingCommandPort;

    @Override
    public UserIsFollowingResponse isFollowing(Long userId, Long targetUserId) {
        boolean isFollowing = followingCommandPort.findByUserIdAndTargetUserId(userId, targetUserId)
                .isPresent();
        return new UserIsFollowingResponse(isFollowing);
    }
}
