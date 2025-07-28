package konkuk.thip.comment.application.port.out;

public interface CommentLikeQueryPort {
    boolean isLikedCommentByUser(Long userId, Long commentId);
}
