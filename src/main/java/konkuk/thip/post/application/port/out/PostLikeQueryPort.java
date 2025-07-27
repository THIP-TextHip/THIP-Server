package konkuk.thip.post.application.port.out;

import java.util.List;

public interface PostLikeQueryPort {

    int countByPostId(Long postId);
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    List<Long> findLikedFeedIdsByUserIdAndFeedIds(Long userId, List<Long> feedIds);
}
