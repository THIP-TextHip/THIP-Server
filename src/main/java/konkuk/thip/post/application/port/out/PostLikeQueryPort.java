package konkuk.thip.post.application.port.out;

import java.util.List;
import java.util.Set;

public interface PostLikeQueryPort {

    int countByPostId(Long postId);
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Set<Long> findLikedFeedIdsByUserIdAndFeedIds(Long userId, List<Long> feedIds);
}
