package konkuk.thip.post.application.port.out;

import java.util.List;

import java.util.Set;

public interface PostLikeQueryPort {

    List<Long> findLikedFeedIdsByUserIdAndFeedIds(Long userId, List<Long> feedIds);
    Set<Long> findPostIdsLikedByUser(Set<Long> postIds, Long userId);
}
