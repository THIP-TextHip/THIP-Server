package konkuk.thip.comment.application.port.in.dto;

public record ChildCommentsShowQuery(
        Long rootCommentId,
        Long userId,
        String cursorStr
) {
    public static ChildCommentsShowQuery of(Long rootCommentId, Long userId, String cursorStr) {
        return new ChildCommentsShowQuery(rootCommentId, userId, cursorStr);
    }
}

