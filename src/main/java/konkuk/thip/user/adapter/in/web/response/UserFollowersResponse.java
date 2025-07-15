package konkuk.thip.user.adapter.in.web.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record UserFollowersResponse(
        List<Follower> followerList,
        int size,
        LocalDateTime nextCursor,
        boolean isFirst,
        boolean isLast
) {
    @Builder
    public record Follower(
            Long userId,
            String nickname,
            String profileImageUrl,
            String aliasName,
            Integer followingCount
    ){
    }

}
