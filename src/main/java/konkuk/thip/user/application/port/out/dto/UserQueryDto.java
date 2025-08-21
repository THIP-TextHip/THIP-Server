package konkuk.thip.user.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import konkuk.thip.user.domain.value.Alias;

import java.time.LocalDateTime;

public record UserQueryDto(Long userId,
                           String nickname,
                           String profileImageUrl,
                           String aliasName,
                           String aliasColor,
                           Integer followerCount,
                           LocalDateTime createdAt) {

    @QueryProjection
    public UserQueryDto (
            Long userId,
            String nickname,
            Alias userAlias,
            Integer followerCount,
            LocalDateTime createdAt
    ){
//        Assert.notNull(userId, "userId must not be null");
//        Assert.notNull(nickname, "nickname must not be null");
//        Assert.notNull(userAlias, "userAlias must not be null");
////        Assert.notNull(followerCount, "followerCount must not be null"); // 내 팔로잉 목록 조회에서는 필요 x
//        Assert.notNull(createdAt, "createdAt must not be null");

        this(
                userId,
                nickname,
                userAlias.getImageUrl(),
                userAlias.getValue(),
                userAlias.getColor(),
                followerCount,
                createdAt
        );
    }
}
