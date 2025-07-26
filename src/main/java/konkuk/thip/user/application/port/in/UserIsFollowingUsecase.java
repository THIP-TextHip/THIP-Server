package konkuk.thip.user.application.port.in;

public interface UserIsFollowingUsecase {
    Boolean isFollowing(Long userId, Long targetUserId);
}
