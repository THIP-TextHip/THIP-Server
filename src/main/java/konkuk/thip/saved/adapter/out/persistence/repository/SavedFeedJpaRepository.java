package konkuk.thip.saved.adapter.out.persistence.repository;

import konkuk.thip.saved.adapter.out.jpa.SavedFeedJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedFeedJpaRepository extends JpaRepository<SavedFeedJpaEntity, Long> {
}
