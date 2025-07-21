package konkuk.thip.saved.adapter.out.persistence.repository;

import konkuk.thip.saved.adapter.out.jpa.SavedFeedJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedFeedJpaRepository extends JpaRepository<SavedFeedJpaEntity, Long> {
    void deleteByUserJpaEntity_UserIdAndFeedJpaEntity_PostId(Long userId, Long feedId);
    List<SavedFeedJpaEntity> findByUserJpaEntity_UserId(Long userId);
}
