package konkuk.thip.feed.adapter.out.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class FeedJpaEntity {

    @Id
    private Long id;
}
