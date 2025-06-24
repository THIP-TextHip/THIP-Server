package konkuk.thip.saved.adapter.out.persistence;

import konkuk.thip.saved.adapter.out.jpa.SavedBookJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedBookRepository extends JpaRepository<SavedBookJpaEntity, Long> {
}