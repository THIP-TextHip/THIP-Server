package konkuk.thip.comment.adapter.in.web.response;

import java.util.List;

public record CommentCreateResponse(
        Long commentId,
        Long creatorId,
        String creatorProfileImageUrl,
        String creatorNickname,
        String aliasName,
        String aliasColor,
        String postDate,        // 댓글 작성 시각 (~ 전 형식)
        String content,
        int likeCount,
        boolean isLike,
        boolean isDeleted,  // 삭제된 댓글인지 아닌지
        boolean isWriter,
        List<ReplyCommentCreateDto> replyList
) {
    public record ReplyCommentCreateDto(
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
