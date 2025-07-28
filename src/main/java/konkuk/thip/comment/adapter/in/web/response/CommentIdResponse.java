package konkuk.thip.comment.adapter.in.web.response;

public record CommentIdResponse(Long commentId) {
    public static CommentIdResponse of(Long commentId) {
        return new CommentIdResponse(commentId);
    }
}
