package konkuk.thip.comment.adapter.out.persistence.repository;

import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, Long>, CommentQueryRepository {

    /**
     * 소프트 딜리트 적용 대상 entity 단건 조회 메서드
     */
    Optional<CommentJpaEntity> findByCommentId(Long commentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CommentJpaEntity c SET c.status = 'INACTIVE' WHERE c.postJpaEntity.postId = :postId")
    void softDeleteAllByPostId(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CommentJpaEntity c SET c.status = 'INACTIVE' WHERE c.userJpaEntity.userId = :userId")
    void softDeleteAllByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM CommentJpaEntity c JOIN FETCH c.postJpaEntity p " +
            "WHERE c.userJpaEntity.userId = :userId")
    List<CommentJpaEntity> findAllCommentsWithPostsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CommentJpaEntity c SET c.status = 'INACTIVE' WHERE c.postJpaEntity.postId IN :postIds")
    void softDeleteAllByPostIds(@Param("postIds") List<Long> postIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CommentJpaEntity c " +
            "SET c.likeCount = CASE WHEN c.likeCount > 0 THEN c.likeCount - 1 ELSE 0 END " +
            "WHERE c.commentId IN :likedCommentIds")
    void bulkDecrementLikeCountByIds(@Param("likedCommentIds") List<Long> likedCommentIds);
}
