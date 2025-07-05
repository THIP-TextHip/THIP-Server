package konkuk.thip.saved.adapter.out.persistence;

import konkuk.thip.saved.adapter.out.jpa.SavedBookJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedBookJpaRepository extends JpaRepository<SavedBookJpaEntity, Long> {
    boolean existsByUserJpaEntity_UserIdAndBookJpaEntity_BookId(Long userId, Long bookId);
    void deleteByUserJpaEntity_UserIdAndBookJpaEntity_BookId(Long userId, Long bookId);
    List<SavedBookJpaEntity> findByUserJpaEntity_UserId(Long userId);
}