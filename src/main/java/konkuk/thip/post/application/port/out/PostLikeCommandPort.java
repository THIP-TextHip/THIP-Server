package konkuk.thip.post.application.port.out;

public interface PostLikeCommandPort {
    int countByPostIdAndUserId(Long postId);
    boolean existsByPostIdAndUserId(Long postId, Long userId);
}
