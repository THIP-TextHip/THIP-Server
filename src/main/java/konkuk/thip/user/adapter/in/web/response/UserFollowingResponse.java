package konkuk.thip.user.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

@Builder
public record UserFollowingResponse(
        List<FollowingDto> followings,
        Integer totalFollowingCount,
        String nextCursor,
        boolean isLast
) {
    @Builder
    public record FollowingDto(
            Long userId,
            String nickname,
            String profileImageUrl,
            String aliasName,
            String aliasColor,
            boolean isFollowing
    ){
    }

}
