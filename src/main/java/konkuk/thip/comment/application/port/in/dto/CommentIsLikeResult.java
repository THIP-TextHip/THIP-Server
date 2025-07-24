package konkuk.thip.comment.application.port.in.dto;

public record CommentIsLikeResult(
        Long commentId,
        boolean isLiked
)
{
    public static CommentIsLikeResult of(Long commentId, boolean isLiked) {
        return new CommentIsLikeResult(commentId, isLiked);
    }
}