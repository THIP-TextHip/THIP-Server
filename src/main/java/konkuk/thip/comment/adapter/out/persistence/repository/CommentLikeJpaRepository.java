package konkuk.thip.comment.adapter.out.persistence.repository;

import konkuk.thip.comment.adapter.out.jpa.CommentLikeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentLikeJpaRepository extends JpaRepository<CommentLikeJpaEntity, Long> {
    @Query(value = "SELECT * FROM comment_likes WHERE user_id = :userId", nativeQuery = true)
    List<CommentLikeJpaEntity> findAllByUserId(Long userId);

    @Modifying
    @Query(value = "DELETE FROM comment_likes WHERE user_id = :userId AND comment_id = :commentId", nativeQuery = true)
    void deleteByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);
}