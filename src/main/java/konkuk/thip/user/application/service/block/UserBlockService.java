package konkuk.thip.user.application.service.block;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.user.application.port.in.UserBlockUseCase;
import konkuk.thip.user.application.port.in.dto.UserBlockCommand;
import konkuk.thip.user.application.port.out.FollowingCommandPort;
import konkuk.thip.user.application.port.out.UserBlockCommandPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import konkuk.thip.user.domain.UserBlock;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static konkuk.thip.common.exception.code.ErrorCode.USER_CANNOT_BLOCK_SELF;

@Service
@RequiredArgsConstructor
public class UserBlockService implements UserBlockUseCase {

    private final UserBlockCommandPort userBlockCommandPort;
    private final FollowingCommandPort followingCommandPort;
    private final UserCommandPort userCommandPort;

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
    public Boolean changeBlockState(UserBlockCommand blockCommand) {
        Long userId = blockCommand.userId();
        Long targetUserId = blockCommand.targetUserId();
        Boolean type = blockCommand.type();

        validateParams(userId, targetUserId);

        boolean isExistingBlock = userBlockCommandPort.findByUserIdAndTargetUserId(userId, targetUserId).isPresent();
        boolean isBlockRequest = UserBlock.validateBlockState(isExistingBlock, type);

        if (!isBlockRequest) { // 차단 해제 요청인 경우 : 팔로우 관계는 복구하지 않는다
            userBlockCommandPort.deleteBlock(UserBlock.withoutId(userId, targetUserId));
            return false;
        }

        // 차단 요청인 경우 : 대상 유저가 실제로 존재하는지 확인하고, 양방향 팔로우를 해제한다
        Map<Long, User> lockedUsers = lockUsersInIdOrder(userId, targetUserId);
        unfollowBothWays(userId, targetUserId, lockedUsers);

        userBlockCommandPort.save(UserBlock.withoutId(userId, targetUserId));
        return true;
    }

    @Recover
    public Boolean recoverChangeBlockState(Exception e, UserBlockCommand blockCommand) {
        throw new BusinessException(ErrorCode.RESOURCE_LOCKED);
    }

    // 데드락 방지 : 항상 userId 오름차순으로 락을 획득한다
    private Map<Long, User> lockUsersInIdOrder(Long userId, Long targetUserId) {
        Long first = Math.min(userId, targetUserId);
        Long second = Math.max(userId, targetUserId);

        User firstUser = userCommandPort.findByIdWithLock(first);
        User secondUser = userCommandPort.findByIdWithLock(second);

        return Map.of(first, firstUser, second, secondUser);
    }

    // 팔로우 관계는 락을 잡은 뒤 조회한다. 락 이전에 읽으면 이미 사라진 관계의 followerCount 를 감소시킬 수 있다.
    private void unfollowBothWays(Long userId, Long targetUserId, Map<Long, User> lockedUsers) {
        followingCommandPort.findByUserIdAndTargetUserId(userId, targetUserId)
                .ifPresent(following -> {
                    User targetUser = lockedUsers.get(targetUserId);
                    targetUser.decreaseFollowerCount();
                    followingCommandPort.deleteFollowing(following, targetUser);
                });

        followingCommandPort.findByUserIdAndTargetUserId(targetUserId, userId)
                .ifPresent(following -> {
                    User user = lockedUsers.get(userId);
                    user.decreaseFollowerCount();
                    followingCommandPort.deleteFollowing(following, user);
                });
    }

    private void validateParams(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BusinessException(USER_CANNOT_BLOCK_SELF);
        }
    }
}
