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
    public record Follower(
            Long userId,
            String nickname,
            String profileImageUrl,
            String aliasName,
            Integer followingCount
    ){
        public static Follower of(Long userId, String nickname, String profileImageUrl, String aliasName, Integer followingCount) {
            return new Follower(
                    userId,
                    nickname,
                    profileImageUrl,
                    aliasName,
                    followingCount
            );
        }
    }

}
