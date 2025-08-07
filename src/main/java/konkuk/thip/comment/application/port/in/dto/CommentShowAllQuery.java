package konkuk.thip.comment.application.port.in.dto;


public record CommentShowAllQuery(
        Long postId,
        Long userId,
        String postType,
        String cursorStr
) {
    public static CommentShowAllQuery of(Long postId, Long userId, String postType, String cursorStr) {
        return new CommentShowAllQuery(postId, userId, postType, cursorStr);
    }
}
