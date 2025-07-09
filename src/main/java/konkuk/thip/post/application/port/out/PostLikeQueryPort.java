package konkuk.thip.post.application.port.out;

public interface PostLikeQueryPort {

    int countByPostId(Long postId);
    boolean existsByPostIdAndUserId(Long postId, Long userId);

}
