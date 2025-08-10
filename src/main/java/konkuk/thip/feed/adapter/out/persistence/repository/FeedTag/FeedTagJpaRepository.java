package konkuk.thip.feed.adapter.out.persistence.repository.FeedTag;

import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.FeedTagJpaEntity;
import konkuk.thip.saved.application.port.out.dto.FeedIdAndTagProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedTagJpaRepository extends JpaRepository<FeedTagJpaEntity, Long>{

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM FeedTagJpaEntity ft WHERE ft.feedJpaEntity = :feedJpaEntity")
    void deleteAllByFeedJpaEntity(@Param("feedJpaEntity") FeedJpaEntity feedJpaEntity);

    @Query("""
    SELECT ft.feedJpaEntity.postId as feedId, ft.tagJpaEntity as tagJpaEntity
    FROM FeedTagJpaEntity ft
    WHERE ft.feedJpaEntity.postId IN :feedIds
    """)
    List<FeedIdAndTagProjection> findFeedIdAndTagsByFeedIds(@Param("feedIds") List<Long> feedIds);
}
