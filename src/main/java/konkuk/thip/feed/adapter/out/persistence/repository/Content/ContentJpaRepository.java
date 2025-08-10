package konkuk.thip.feed.adapter.out.persistence.repository.Content;

import konkuk.thip.feed.adapter.out.jpa.ContentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentJpaRepository extends JpaRepository<ContentJpaEntity, Long>{

    @Modifying
    @Query("DELETE FROM ContentJpaEntity c WHERE c.postJpaEntity.postId = :feedId")
    void deleteAllByFeedId(@Param("feedId") Long feedId);
}
