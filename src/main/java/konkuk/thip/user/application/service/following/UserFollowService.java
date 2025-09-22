package konkuk.thip.user.application.service.following;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.notification.application.port.in.FeedNotificationOrchestrator;
import konkuk.thip.user.application.port.in.UserFollowUsecase;
import konkuk.thip.user.application.port.in.dto.UserFollowCommand;
import konkuk.thip.user.application.port.out.FollowingCommandPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.Following;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserFollowService implements UserFollowUsecase {

    private final FollowingCommandPort followingCommandPort;
    private final UserCommandPort userCommandPort;

    private final FeedNotificationOrchestrator feedNotificationOrchestrator;

    @Override
    @Transactional
    public Boolean changeFollowingState(UserFollowCommand followCommand) {
        Long userId = followCommand.userId();
        Long targetUserId = followCommand.targetUserId();
        Boolean type = followCommand.type();

        validateParams(userId, targetUserId);

        Optional<Following> optionalFollowing = followingCommandPort.findByUserIdAndTargetUserId(userId, targetUserId);
        User targetUser = userCommandPort.findById(targetUserId);

        boolean isFollowRequest = Following.validateFollowingState(optionalFollowing.isPresent(), type);

        if (isFollowRequest) { // 팔로우 요청인 경우
            targetUser.increaseFollowerCount();
            followingCommandPort.save(Following.withoutId(userId, targetUserId), targetUser);

            // 팔로우 푸쉬알림 전송
            sendNotifications(userId, targetUserId);
            return true;
        } else { // 언팔로우 요청인 경우
            targetUser.decreaseFollowerCount();
            followingCommandPort.deleteFollowing(optionalFollowing.get(), targetUser);
            return false;
        }
    }

    private void sendNotifications(Long userId, Long targetUserId) {
        User actorUser = userCommandPort.findById(userId);
        feedNotificationOrchestrator.notifyFollowed(targetUserId, actorUser.getId(), actorUser.getNickname());
    }

    private void validateParams(Long userId, Long targetUserId) {
        if(userId.equals(targetUserId)) {
            throw new BusinessException(USER_CANNOT_FOLLOW_SELF);
        }
    }
}
