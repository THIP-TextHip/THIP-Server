package konkuk.thip.post.application.port.out;

import java.util.Set;

public interface PostLikeQueryPort {

    Set<Long> findPostIdsLikedByUser(Set<Long> postIds, Long userId);
}
