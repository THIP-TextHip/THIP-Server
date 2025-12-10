package konkuk.thip.post.application.port.out;

import java.util.Set;
import konkuk.thip.post.domain.PostType;

public interface PostLikeRedisCommandPort {
    void updateLikeCount(PostType postType, Long postId, Integer likeCount, boolean isLike);
    void bulkResetLikeCounts(Set<String> keysToReset);
    void addLikeRecordToSet(Long userId, Long postId);
    void removeLikeRecordFromSet(Long userId, Long postId);
}
