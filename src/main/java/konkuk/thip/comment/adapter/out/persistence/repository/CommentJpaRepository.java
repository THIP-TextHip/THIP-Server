package konkuk.thip.comment.adapter.out.persistence.repository;

import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, Long>, CommentQueryRepository {

    /**
     * 소프트 딜리트 적용 대상 entity 단건 조회 메서드
     */
    Optional<CommentJpaEntity> findByCommentId(Long commentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CommentJpaEntity c SET c.status = 'INACTIVE' WHERE c.postJpaEntity.postId = :postId")
    void softDeleteAllByPostId(@Param("postId") Long postId);

}
