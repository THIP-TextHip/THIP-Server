package konkuk.thip.user.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

@Builder
public record UserFollowersResponse(
        List<Follower> followers,
        String nextCursor,
        boolean isLast
) {
    @Builder
    public record Follower(
            Long userId,
            String nickname,
            String profileImageUrl,
            String aliasName,
            Integer followerCount
    ){
    }

}
