package konkuk.thip.comment.adapter.out.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class CommentJpaEntity {

    @Id
    private Long id;
}
