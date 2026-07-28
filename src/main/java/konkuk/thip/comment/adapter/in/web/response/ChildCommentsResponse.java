package konkuk.thip.comment.adapter.in.web.response;

import java.util.List;

public record ChildCommentsResponse(
        List<ChildCommentDto> childComments,
        String nextCursor,
        boolean isLast
) {
    public record ChildCommentDto(
            Long commentId,
            String parentCommentCreatorNickname,
            Long creatorId,
            String creatorProfileImageUrl,
            String creatorNickname,
            String aliasName,
            String aliasColor,
            String postDate,        // 댓글 작성 시각 (~ 전 형식)
            String content,
            int likeCount,
            boolean isLike,
            boolean isWriter
    ) {}
}

