package konkuk.thip.feed.adapter.out.persistence.repository.Tag;

import konkuk.thip.feed.adapter.out.jpa.TagJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagJpaRepository extends JpaRepository<TagJpaEntity, Long>{
    Optional<TagJpaEntity> findByValue(String value);

    @Query("SELECT ft.tagJpaEntity FROM FeedTagJpaEntity ft WHERE ft.feedJpaEntity.postId = :feedId")
    List<TagJpaEntity> findAllByFeedId(@Param("feedId") Long feedId);

}
