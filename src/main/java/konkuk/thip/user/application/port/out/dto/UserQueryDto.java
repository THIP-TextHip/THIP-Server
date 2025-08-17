package konkuk.thip.user.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

public record UserQueryDto(Long userId,
                           String nickname,
                           String profileImageUrl,
                           String aliasName,
                           String aliasColor,
                           Integer followerCount,
                           LocalDateTime createdAt) {

    @QueryProjection
    public UserQueryDto {
        Assert.notNull(userId, "userId must not be null");
        Assert.notNull(nickname, "nickname must not be null");
        Assert.notNull(profileImageUrl, "profileImageUrl must not be null");
        Assert.notNull(aliasName, "aliasName must not be null");
        Assert.notNull(aliasColor, "aliasColor must not be null");
//        Assert.notNull(followerCount, "followerCount must not be null"); // 내 팔로잉 목록 조회에서는 필요 x
        Assert.notNull(createdAt, "createdAt must not be null");
    }
}
