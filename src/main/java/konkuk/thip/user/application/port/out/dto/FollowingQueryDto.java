package konkuk.thip.user.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record FollowingQueryDto(
        Long userId,
        Long followingTargetUserId,
        String followingUserNickname,
        String followingUserProfileImageUrl,
        LocalDateTime followedAt
) {
    @QueryProjection
    public FollowingQueryDto {}
}
