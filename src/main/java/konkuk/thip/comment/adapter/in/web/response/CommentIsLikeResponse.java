package konkuk.thip.comment.adapter.in.web.response;

import konkuk.thip.comment.application.port.in.dto.CommentIsLikeResult;

public record CommentIsLikeResponse(
        Long commentId,
        boolean isLiked
) {
        public static CommentIsLikeResponse of(CommentIsLikeResult commentIsLikeResult) {
                return new CommentIsLikeResponse(commentIsLikeResult.commentId(), commentIsLikeResult.isLiked());
        }
}
