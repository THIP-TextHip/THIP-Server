package konkuk.thip.comment.application.port.in.dto;

public record RootCommentCreateCommand(
        String content,
        String postType,
        Long postId,
        Long userId
) {
}
