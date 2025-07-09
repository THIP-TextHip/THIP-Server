package konkuk.thip.comment.application.port.out;


public interface CommentCommandPort {

    int countByPostIdAndUserId(Long postId, Long userId);

}
