package konkuk.thip.comment.application.port.out.dto;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.annotation.Nullable;
import konkuk.thip.user.domain.Alias;

import java.time.LocalDateTime;

public record CommentQueryDto(
        Long commentId,
        @Nullable Long parentCommentId,
        @Nullable String parentCommentCreatorNickname,
        Long creatorId,
        String creatorProfileImageUrl,
        String creatorNickname,
        String alias,
        String aliasColor,
        LocalDateTime createdAt,    // 댓글 작성 시각
        String content,
        int likeCount,
        Boolean isDeleted
) {
    /**
     * child comment
     */
    @QueryProjection
    public CommentQueryDto (
            Long commentId,
            Long parentCommentId,
            String parentCommentCreatorNickname,
            Long creatorId,
            Alias creatorAlias,
            String creatorNickname,
            LocalDateTime createdAt,    // 댓글 작성 시각
            String content,
            int likeCount,
            Boolean isDeleted
    ) {
        this(commentId, parentCommentId, parentCommentCreatorNickname, creatorId, creatorAlias.getImageUrl(),
                creatorNickname, creatorAlias.getValue(), creatorAlias.getColor(),
                createdAt, content, likeCount, isDeleted);
    }

    /**
     * root comment
     */
    @QueryProjection
    public CommentQueryDto (
            Long commentId,
            Long creatorId,
            Alias creatorAlias,
            String creatorNickname,
            LocalDateTime createdAt,    // 댓글 작성 시각
            String content,
            int likeCount,
            boolean isDeleted
    ) {
        this(commentId, null, null, creatorId, creatorAlias.getImageUrl(),
                creatorNickname, creatorAlias.getValue(), creatorAlias.getColor(),
                createdAt, content, likeCount, isDeleted);
    }
}
