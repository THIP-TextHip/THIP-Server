package konkuk.thip.book.adapter.out.persistence.repository;

import konkuk.thip.book.adapter.out.jpa.SavedBookJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SavedBookJpaRepository extends JpaRepository<SavedBookJpaEntity, Long> {
    boolean existsByUserJpaEntity_UserIdAndBookJpaEntity_BookId(Long userId, Long bookId);
    void deleteByUserJpaEntity_UserIdAndBookJpaEntity_BookId(Long userId, Long bookId);

    @Query("SELECT s FROM SavedBookJpaEntity s WHERE s.userJpaEntity.userId = :userId")
    List<SavedBookJpaEntity> findByUserId(Long userId);
}