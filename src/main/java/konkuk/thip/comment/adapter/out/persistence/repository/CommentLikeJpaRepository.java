package konkuk.thip.comment.adapter.out.persistence.repository;

import konkuk.thip.comment.adapter.out.jpa.CommentLikeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface CommentLikeJpaRepository extends JpaRepository<CommentLikeJpaEntity, Long> {

    @Query("SELECT cl FROM CommentLikeJpaEntity cl WHERE cl.userJpaEntity.userId = :userId")
    List<CommentLikeJpaEntity> findAllByUserId(@Param("userId") Long userId);


    @Modifying
    @Query("DELETE FROM CommentLikeJpaEntity cl WHERE cl.userJpaEntity.userId = :userId AND cl.commentJpaEntity.commentId = :commentId")
    void deleteByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);

    @Query("SELECT CASE WHEN COUNT(cl) > 0 THEN true ELSE false END " +
            "FROM CommentLikeJpaEntity cl " +
            "WHERE cl.userJpaEntity.userId = :userId AND cl.commentJpaEntity.commentId = :commentId")
    boolean existsByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);

    @Modifying
    @Query("DELETE FROM CommentLikeJpaEntity cl WHERE cl.commentJpaEntity.commentId = :commentId")
    void deleteAllByCommentId(@Param("commentId") Long commentId);

    @Query("SELECT c.commentJpaEntity.commentId FROM CommentLikeJpaEntity c WHERE c.userJpaEntity.userId = :userId AND c.commentJpaEntity.commentId IN :commentIds")
    Set<Long> findCommentIdsLikedByUser(@Param("commentIds") Set<Long> commentIds, @Param("userId") Long userId);
}
