package konkuk.thip.comment.adapter.in.web.response;

public record CommentDeleteResponse(Long postId) {
    public static CommentDeleteResponse of(Long postId) {
        return new CommentDeleteResponse(postId);
    }
}
