package konkuk.thip.user.application.service.following;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.notification.application.port.in.FeedNotificationOrchestrator;
import konkuk.thip.user.application.port.in.UserFollowUsecase;
import konkuk.thip.user.application.port.in.dto.UserFollowCommand;
import konkuk.thip.user.application.port.out.FollowingCommandPort;
import konkuk.thip.user.application.port.out.UserBlockQueryPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.Following;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.USER_BLOCKED_CANNOT_INTERACT;
import static konkuk.thip.common.exception.code.ErrorCode.USER_CANNOT_FOLLOW_SELF;

@Service
@RequiredArgsConstructor
public class UserFollowService implements UserFollowUsecase {

    private final FollowingCommandPort followingCommandPort;
    private final UserCommandPort userCommandPort;
    private final UserBlockQueryPort userBlockQueryPort;

    private final FeedNotificationOrchestrator feedNotificationOrchestrator;

    @Override
    @Transactional
    @Retryable(
            notRecoverable = {
                    BusinessException.class,
                    InvalidStateException.class
            },
            noRetryFor = {
                    BusinessException.class,
                    InvalidStateException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, maxDelay = 500, multiplier = 2)
    )
    public Boolean changeFollowingState(UserFollowCommand followCommand) {
        Long userId = followCommand.userId();
        Long targetUserId = followCommand.targetUserId();
        Boolean type = followCommand.type();

        validateParams(userId, targetUserId);

        Optional<Following> optionalFollowing = followingCommandPort.findByUserIdAndTargetUserId(userId, targetUserId);
        User targetUser = userCommandPort.findByIdWithLock(targetUserId);

        boolean isFollowRequest = Following.validateFollowingState(optionalFollowing.isPresent(), type);

        if (isFollowRequest) { // 팔로우 요청인 경우
            validateNotBlocked(userId, targetUserId);
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

    @Recover
    public Boolean recoverChangeFollowingState(Exception e, UserFollowCommand followCommand) {
        throw new BusinessException(ErrorCode.RESOURCE_LOCKED);
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

    private void validateNotBlocked(Long userId, Long targetUserId) {
        if (userBlockQueryPort.existsBlockBetween(userId, targetUserId)) {
            throw new BusinessException(USER_BLOCKED_CANNOT_INTERACT);
        }
    }
}
