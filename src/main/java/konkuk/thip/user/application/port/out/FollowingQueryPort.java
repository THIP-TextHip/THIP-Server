package konkuk.thip.user.application.port.out;

public interface FollowingQueryPort {
    int countByFollowingUserId(Long followingUserId);
}

