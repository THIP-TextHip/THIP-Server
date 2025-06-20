package konkuk.thip.domain.comment.adapter.out.persistence;

import konkuk.thip.domain.comment.adapter.out.jpa.CommentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, Long> {
}
