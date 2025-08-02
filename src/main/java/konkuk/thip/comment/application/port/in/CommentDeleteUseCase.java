package konkuk.thip.comment.application.port.in;


public interface CommentDeleteUseCase {
    void deleteComment(Long commentId, Long userId);
}
