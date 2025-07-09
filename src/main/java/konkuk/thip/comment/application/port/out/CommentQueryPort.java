package konkuk.thip.comment.application.port.out;

public interface CommentQueryPort {

    int countByPostIdAndUserId(Long postId, Long userId);

}
