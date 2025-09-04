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


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CommentLikeJpaEntity cl WHERE cl.userJpaEntity.userId = :userId AND cl.commentJpaEntity.commentId = :commentId")
    void deleteByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);

    @Query("SELECT CASE WHEN COUNT(cl) > 0 THEN true ELSE false END " +
            "FROM CommentLikeJpaEntity cl " +
            "WHERE cl.userJpaEntity.userId = :userId AND cl.commentJpaEntity.commentId = :commentId")
    boolean existsByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CommentLikeJpaEntity cl WHERE cl.commentJpaEntity.commentId = :commentId")
    void deleteAllByCommentId(@Param("commentId") Long commentId);

    @Query("SELECT c.commentJpaEntity.commentId FROM CommentLikeJpaEntity c WHERE c.userJpaEntity.userId = :userId AND c.commentJpaEntity.commentId IN :commentIds")
    Set<Long> findCommentIdsLikedByUser(@Param("commentIds") Set<Long> commentIds, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           DELETE FROM CommentLikeJpaEntity cl
           WHERE cl.commentJpaEntity.commentId IN (
                SELECT c.commentId FROM CommentJpaEntity c
                WHERE c.postJpaEntity.postId = :postId
           )
           """)
    void deleteAllByPostId(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
       DELETE FROM CommentLikeJpaEntity cl
       WHERE cl.commentJpaEntity.commentId IN (
           SELECT c.commentId FROM CommentJpaEntity c
           WHERE c.userJpaEntity.userId = :userId
       )
       """)
    void deleteAllByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("SELECT cl.commentJpaEntity.commentId FROM CommentLikeJpaEntity cl WHERE cl.userJpaEntity.userId = :userId")
    List<Long> findAllCommentIdsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
       DELETE FROM CommentLikeJpaEntity cl
       WHERE cl.commentJpaEntity.commentId IN (
           SELECT c.commentId FROM CommentJpaEntity c
           WHERE c.postJpaEntity.postId IN :postIds
       )
       """)
    void deleteAllByPostIds(@Param("postIds") List<Long> postIds);
}
