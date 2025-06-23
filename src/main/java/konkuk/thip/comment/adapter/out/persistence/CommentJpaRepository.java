package konkuk.thip.comment.adapter.out.persistence;

import konkuk.thip.comment.adapter.out.jpa.CommentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, Long> {
}
