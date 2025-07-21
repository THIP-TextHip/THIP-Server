package konkuk.thip.feed.adapter.out.persistence.repository.FeedTag;

import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import konkuk.thip.feed.adapter.out.jpa.FeedTagJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedTagJpaRepository extends JpaRepository<FeedTagJpaEntity, Long>{

    @Modifying
    @Query("DELETE FROM FeedTagJpaEntity ft WHERE ft.feedJpaEntity = :feedJpaEntity")
    void deleteAllByFeedJpaEntity(@Param("feedJpaEntity") FeedJpaEntity feedJpaEntity);

    @Query("""
    SELECT ft.feedJpaEntity.postId, ft.tagJpaEntity
    FROM FeedTagJpaEntity ft
    WHERE ft.feedJpaEntity.postId IN :feedIds
    """)
    List<Object[]> findFeedIdAndTagsByFeedIds(@Param("feedIds") List<Long> feedIds);


}
