package konkuk.thip.domain.feed.adapter.out.persistence;

import konkuk.thip.domain.feed.adapter.out.jpa.FeedJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedJpaRepository extends JpaRepository<FeedJpaEntity, Long> {
}
