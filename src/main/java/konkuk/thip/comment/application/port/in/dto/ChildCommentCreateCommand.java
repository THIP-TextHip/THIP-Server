package konkuk.thip.comment.application.port.in.dto;

public record ChildCommentCreateCommand(
        String content,
        String postType,
        Long postId,
        Long userId,
        Long parentCommentId
) {
}
