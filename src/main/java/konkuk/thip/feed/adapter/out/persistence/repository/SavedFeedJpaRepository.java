package konkuk.thip.feed.adapter.out.persistence.repository;

import konkuk.thip.feed.adapter.out.jpa.SavedFeedJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface SavedFeedJpaRepository extends JpaRepository<SavedFeedJpaEntity, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SavedFeedJpaEntity sf WHERE sf.userJpaEntity.userId = :userId AND sf.feedJpaEntity.postId = :feedId")
    void deleteByUserIdAndFeedId(@Param("userId") Long userId, @Param("feedId") Long feedId);

    @Query("SELECT sf FROM SavedFeedJpaEntity sf WHERE sf.userJpaEntity.userId = :userId")
    List<SavedFeedJpaEntity> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT s.feedJpaEntity.postId FROM SavedFeedJpaEntity s WHERE s.userJpaEntity.userId = :userId AND s.feedJpaEntity.postId IN :feedIds")
    Set<Long> findSavedFeedIdsByUserIdAndFeedIds(@Param("userId") Long userId, @Param("feedIds") Set<Long> feedIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SavedFeedJpaEntity sf WHERE sf.feedJpaEntity.postId = :feedId")
    int deleteAllByFeedId(@Param("feedId") Long feedId);
}
