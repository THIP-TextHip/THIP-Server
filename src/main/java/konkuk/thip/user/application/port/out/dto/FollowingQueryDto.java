package konkuk.thip.user.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import konkuk.thip.user.domain.value.Alias;

import java.time.LocalDateTime;

public record FollowingQueryDto(
        Long userId,
        Long followingTargetUserId,
        String followingUserNickname,
        String followingUserProfileImageUrl,
        LocalDateTime followedAt
) {
    @QueryProjection
    public FollowingQueryDto (
        Long userId,
        Long followingTargetUserId,
        String followingUserNickname,
        Alias followingUserAlias,
        LocalDateTime followedAt
    ){
        this(
                userId,
                followingTargetUserId,
                followingUserNickname,
                followingUserAlias.getImageUrl(),
                followedAt
        );
    }
}
