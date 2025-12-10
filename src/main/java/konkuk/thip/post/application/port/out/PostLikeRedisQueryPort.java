package konkuk.thip.post.application.port.out;

import java.util.Map;
import konkuk.thip.post.domain.PostType;

public interface PostLikeRedisQueryPort {
    Integer getLikeCount(PostType postType, Long postId, Integer dbLikeCount);
    Map<String, Integer> getAllLikeCounts();
    boolean isLikedPostByUser(Long userId, Long postId);
}
