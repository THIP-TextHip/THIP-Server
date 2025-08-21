package konkuk.thip.roompost.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import konkuk.thip.user.domain.value.Alias;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

public record RoomPostQueryDto(
        Long postId,
        String postType,
        LocalDateTime postDate,
        Integer page,
        Long userId,
        String nickName,
        String profileImageUrl,
        String content,
        Integer likeCount,
        Integer commentCount,
        Boolean isOverview
) {
    // 정식 생성자(컴팩트)에서 모든 필드 검증
    public RoomPostQueryDto {
        Assert.notNull(postId, "postId must not be null");
        Assert.notNull(postType, "postType must not be null");
        Assert.notNull(postDate, "postDate must not be null");
        Assert.notNull(page, "page must not be null");
        Assert.notNull(userId, "userId must not be null");
        Assert.notNull(nickName, "nickName must not be null");
        Assert.notNull(profileImageUrl, "profileImageUrl must not be null");
        Assert.notNull(content, "content must not be null");
        Assert.notNull(likeCount, "likeCount must not be null");
        Assert.notNull(commentCount, "commentCount must not be null");
        Assert.notNull(isOverview, "isOverview must not be null");
    }

    @QueryProjection
    public RoomPostQueryDto (
            Long postId,
            String postType,
            LocalDateTime postDate,
            Integer page,
            Long userId,
            String nickName,
            Alias alias,
            String content,
            Integer likeCount,
            Integer commentCount,
            Boolean isOverview
    ){
        this(
                postId,
                postType,
                postDate,
                page,
                userId,
                nickName,
                alias.getImageUrl(),
                content,
                likeCount,
                commentCount,
                isOverview
        );
    }
}
