package konkuk.thip.comment.adapter.out.persistence.repository;

import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import konkuk.thip.common.entity.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, Long>, CommentQueryRepository {
    Optional<CommentJpaEntity> findByCommentIdAndStatus(Long commentId, StatusType status);
}
