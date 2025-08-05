package konkuk.thip.user.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

@Builder
public record UserFollowersResponse(
        List<FollowerDto> followers,
        Integer totalFollowerCount,
        String nextCursor,
        boolean isLast
) {
    @Builder
    public record FollowerDto(
            Long userId,
            String nickname,
            String profileImageUrl,
            String aliasName,
            String aliasColor,
            Integer followerCount
    ){

    }

}
