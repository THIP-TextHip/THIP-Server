package konkuk.thip.book.adapter.out.persistence.repository;

import konkuk.thip.book.adapter.out.jpa.SavedBookJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SavedBookJpaRepository extends JpaRepository<SavedBookJpaEntity, Long> {
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SavedBookJpaEntity s " +
           "WHERE s.userJpaEntity.userId = :userId AND s.bookJpaEntity.bookId = :bookId")
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SavedBookJpaEntity s WHERE s.userJpaEntity.userId = :userId AND s.bookJpaEntity.bookId = :bookId")
    void deleteByUserIdAndBookId(Long userId, Long bookId);
}