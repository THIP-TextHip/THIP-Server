package konkuk.thip.post.application.port.out;

import java.util.Set;
import konkuk.thip.post.domain.PostType;

public interface PostLikeCountRedisCommandPort {
    void updateLikeCount(PostType postType, Long postId, Integer likeCount, boolean isLike);
    void bulkResetLikeCounts(Set<String> keysToReset);
}
