package konkuk.thip.user.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

public record ReactionQueryDto(
    String label,
    Long id, // feedId 또는 postId
    String writer,
    Long userId,
    String type,
    String content,
    LocalDateTime createdAt
) {
    @QueryProjection
    public ReactionQueryDto {
        Assert.notNull(label, "label must not be null");
        Assert.notNull(id, "id must not be null");
        Assert.notNull(writer, "writer must not be null");
        Assert.notNull(userId, "userId must not be null");
        Assert.notNull(type, "type must not be null");
        Assert.notNull(content, "content must not be null");
        Assert.notNull(createdAt, "createdAt must not be null");
    }
}
