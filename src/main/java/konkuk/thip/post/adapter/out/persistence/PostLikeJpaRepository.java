package konkuk.thip.post.adapter.out.persistence;

import konkuk.thip.post.adapter.out.jpa.PostLikeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PostLikeJpaRepository extends JpaRepository<PostLikeJpaEntity, Long> {

    @Query("SELECT p.postJpaEntity.postId FROM PostLikeJpaEntity p WHERE p.userJpaEntity.userId = :userId AND p.postJpaEntity.postId IN :postIds")
    Set<Long> findPostIdsLikedByUser(@Param("postIds") Set<Long> postIds,
                                     @Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(pl) > 0 THEN true ELSE false END " +
            "FROM PostLikeJpaEntity pl " +
            "WHERE pl.userJpaEntity.userId = :userId AND pl.postJpaEntity.postId = :postId")
    boolean existsByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PostLikeJpaEntity pl WHERE pl.userJpaEntity.userId = :userId AND pl.postJpaEntity.postId = :postId")
    void deleteByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    @Modifying
    @Query("DELETE FROM PostLikeJpaEntity pl WHERE pl.postJpaEntity.postId = :postId")
    void deleteAllByPostId(@Param("postId") Long postId);
}
