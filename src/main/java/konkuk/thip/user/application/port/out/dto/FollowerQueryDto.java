package konkuk.thip.user.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.jsonwebtoken.lang.Assert;

import java.time.LocalDateTime;

public record FollowerQueryDto(Long userId,
                               String nickname,
                               String profileImageUrl,
                               String aliasName,
                               Integer followerCount,
                               LocalDateTime createdAt) {

    @QueryProjection
    public FollowerQueryDto {
        Assert.notNull(userId, "userId must not be null");
        Assert.notNull(nickname, "nickname must not be null");
        Assert.notNull(profileImageUrl, "profileImageUrl must not be null");
        Assert.notNull(aliasName, "aliasName must not be null");
        Assert.notNull(followerCount, "followerCount must not be null");
        Assert.notNull(createdAt, "createdAt must not be null");
    }
}