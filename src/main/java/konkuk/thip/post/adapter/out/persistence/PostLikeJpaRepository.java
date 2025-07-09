package konkuk.thip.post.adapter.out.persistence;

import konkuk.thip.post.adapter.out.jpa.PostLikeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeJpaRepository extends JpaRepository<PostLikeJpaEntity, Long> {
    int countByPostJpaEntity_PostId(Long postId);

    boolean existsByPostJpaEntity_PostIdAndUserJpaEntity_UserId(Long postId, Long userId);
}
