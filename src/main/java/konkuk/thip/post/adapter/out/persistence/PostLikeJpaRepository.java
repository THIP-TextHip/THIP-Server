package konkuk.thip.post.adapter.out.persistence;

import konkuk.thip.post.adapter.out.jpa.PostLikeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Set;

@Repository
public interface PostLikeJpaRepository extends JpaRepository<PostLikeJpaEntity, Long> {
    int countByPostJpaEntity_PostId(Long postId);

    boolean existsByPostJpaEntity_PostIdAndUserJpaEntity_UserId(Long postId, Long userId);

    @Query("SELECT p.postJpaEntity.postId FROM PostLikeJpaEntity p WHERE p.userJpaEntity.userId = :userId AND p.postJpaEntity.postId IN :postIds")
    List<Long> findPostIdsLikedByUserIdAndPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

    @Query(value = "SELECT pl.post_id FROM post_likes pl WHERE pl.user_id = :userId AND pl.post_id IN (:postIds)", nativeQuery = true)
    Set<Long> findPostIdsLikedByUser(@Param("postIds") Set<Long> postIds,
                                     @Param("userId") Long userId);
}
