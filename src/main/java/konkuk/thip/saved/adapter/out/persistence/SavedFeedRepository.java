package konkuk.thip.saved.adapter.out.persistence;

import konkuk.thip.saved.adapter.out.jpa.SavedFeedJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedFeedRepository extends JpaRepository<SavedFeedJpaEntity, Long> {
}
