package konkuk.thip.user.application.port.in;

public interface UserIsFollowingUsecase {
    boolean isFollowing(Long userId, Long targetUserId);
}
