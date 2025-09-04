package konkuk.thip.comment.application.port.out;


public interface CommentLikeCommandPort {
    void save(Long userId, Long commentId);
    void delete(Long userId, Long commentId);
    void deleteAllByCommentId(Long commentId);
    void deleteAllByUserId(Long userId);
}
