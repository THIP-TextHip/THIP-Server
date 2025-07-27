package konkuk.thip.saved.adapter.out.persistence.repository;

import konkuk.thip.saved.adapter.out.jpa.SavedFeedJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SavedFeedJpaRepository extends JpaRepository<SavedFeedJpaEntity, Long> {
    @Modifying
    @Query(value = "DELETE FROM saved_feeds WHERE user_id = :userId AND post_id = :feedId", nativeQuery = true)
    void deleteByUserIdAndFeedId(@Param("userId") Long userId, @Param("feedId") Long feedId);

    @Query(value = "SELECT * FROM saved_feeds WHERE user_id = :userId", nativeQuery = true)
    List<SavedFeedJpaEntity> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT s.feedJpaEntity.postId FROM SavedFeedJpaEntity s WHERE s.userJpaEntity.userId = :userId AND s.feedJpaEntity.postId IN :feedIds")
    List<Long> findSavedFeedIdsByUserIdAndFeedIds(@Param("userId") Long userId, @Param("feedIds") List<Long> feedIds);
}
