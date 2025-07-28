package konkuk.thip.post.adapter.out.persistence;

import konkuk.thip.post.application.port.out.PostLikeQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Set;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostLikeQueryPersistenceAdapter implements PostLikeQueryPort {

    private final PostLikeJpaRepository postLikeJpaRepository;

    @Override
    public Set<Long> findPostIdsLikedByUser(Set<Long> postIds, Long userId) {
        return postLikeJpaRepository.findPostIdsLikedByUser(postIds, userId);
    }

    @Override
    public List<Long> findLikedFeedIdsByUserIdAndFeedIds(Long userId, List<Long> feedIds) {
        return postLikeJpaRepository.findPostIdsLikedByUserIdAndPostIds(userId, feedIds);
    }
}
