package konkuk.thip.feed.adapter.out.persistence;

import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedJpaRepository extends JpaRepository<FeedJpaEntity, Long>, FeedQueryRepository{
}
