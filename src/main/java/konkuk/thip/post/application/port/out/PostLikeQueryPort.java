package konkuk.thip.post.application.port.out;

import java.util.Set;
import java.util.List;

public interface PostLikeQueryPort {

    Set<Long> findPostIdsLikedByUser(Set<Long> postIds, Long userId);

    Set<Long> findLikedFeedIdsByUserIdAndFeedIds(Long userId, List<Long> feedIds);
}
