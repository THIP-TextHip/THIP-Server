package konkuk.thip.post.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.annotation.Nullable;

public record PostQueryDto(
        Long postId,
        Long creatorId,
        String postType,
        @Nullable Integer page,
        @Nullable Long roomId
) {
    @QueryProjection
    public PostQueryDto(
            Long postId,
            Long creatorId,
            String postType,
            @Nullable Integer page,
            @Nullable Long roomId
    ) {
        this.postId = postId;
        this.creatorId = creatorId;
        this.postType = postType;
        this.page = page;
        this.roomId = roomId;
    }


}
