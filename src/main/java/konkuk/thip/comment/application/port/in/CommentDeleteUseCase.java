package konkuk.thip.comment.application.port.in;


public interface CommentDeleteUseCase {
    Long deleteComment(Long commentId, Long userId);
}
