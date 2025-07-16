package konkuk.thip.user.application.service.following;

import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.user.application.port.in.UserFollowUsecase;
import konkuk.thip.user.application.port.in.dto.UserFollowCommand;
import konkuk.thip.user.application.port.out.FollowingCommandPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.Following;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.USER_ALREADY_UNFOLLOWED;
import static konkuk.thip.common.exception.code.ErrorCode.USER_CANNOT_FOLLOW_SELF;

@Service
@RequiredArgsConstructor
public class UserFollowService implements UserFollowUsecase {

    private final FollowingCommandPort followingCommandPort;
    private final UserCommandPort userCommandPort;

    @Override
    @Transactional
    public Boolean changeFollowingState(UserFollowCommand followCommand) {
        Long userId = followCommand.userId();
        Long targetUserId = followCommand.targetUserId();
        Boolean type = followCommand.type();

        validateParams(userId, targetUserId);

        Optional<Following> optionalFollowing = followingCommandPort.findByUserIdAndTargetUserId(userId, targetUserId);
        User targetUser = userCommandPort.findById(targetUserId);

        if (optionalFollowing.isPresent()) { // 이미 팔로우 관계가 존재하는 경우
            Following following = optionalFollowing.get();
            boolean isFollowing = following.changeFollowingState(type);
            targetUser.updateFollowerCount(isFollowing);
            followingCommandPort.updateStatus(following, targetUser);
            return isFollowing;
        } else { // 팔로우 관계가 존재하지 않는 경우
            if (!type) {
                throw new InvalidStateException(USER_ALREADY_UNFOLLOWED); // 언팔로우 요청인데 팔로우 관계가 존재하지 않으므로 이미 언팔로우 상태
            }
            targetUser.increaseFollowerCount();
            followingCommandPort.save(Following.withoutId(userId, targetUserId), targetUser);
            return true; // 새로 팔로우한 경우
        }
    }

    private void validateParams(Long userId, Long targetUserId) {
        if(Objects.equals(userId, targetUserId)) {
            throw new InvalidStateException(USER_CANNOT_FOLLOW_SELF);
        }
    }
}
