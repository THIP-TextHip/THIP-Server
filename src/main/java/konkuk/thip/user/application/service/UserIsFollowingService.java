package konkuk.thip.user.application.service;

import konkuk.thip.user.application.port.in.UserIsFollowingUsecase;
import konkuk.thip.user.application.port.out.FollowingCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserIsFollowingService implements UserIsFollowingUsecase {

    private final FollowingCommandPort followingCommandPort;

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long userId, Long targetUserId) {
        return followingCommandPort.findByUserIdAndTargetUserId(userId, targetUserId)
                .isPresent();
    }
}
