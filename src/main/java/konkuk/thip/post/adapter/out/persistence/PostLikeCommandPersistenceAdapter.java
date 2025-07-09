package konkuk.thip.post.adapter.out.persistence;

import konkuk.thip.post.application.port.out.PostLikeCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostLikeCommandPersistenceAdapter implements PostLikeCommandPort {

    private final PostLikeJpaRepository postLikeJpaRepository;

    @Override
    public int countByPostIdAndUserId(Long postId) {
        return postLikeJpaRepository.countByPostJpaEntity_PostId(postId);
    }

    @Override
    public boolean existsByPostIdAndUserId(Long postId, Long userId) {
        return postLikeJpaRepository.existsByPostJpaEntity_PostIdAndUserJpaEntity_UserId(postId, userId);
    }
}
