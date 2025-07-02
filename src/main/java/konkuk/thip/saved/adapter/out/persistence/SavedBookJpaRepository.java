package konkuk.thip.saved.adapter.out.persistence;

import konkuk.thip.saved.adapter.out.jpa.SavedBookJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedBookJpaRepository extends JpaRepository<SavedBookJpaEntity, Long> {
    boolean existsByUserJpaEntity_UserIdAndBookJpaEntity_BookId(Long userId, Long bookId);
}