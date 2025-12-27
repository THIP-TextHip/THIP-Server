package konkuk.thip.user.application.port.out;

public interface FollowingEventCommandPort {
    void publishUserFollowedEvent(Long userId, Long targetUserId);
    void publishUserUnfollowedEvent(Long userId, Long targetUserId);
}
