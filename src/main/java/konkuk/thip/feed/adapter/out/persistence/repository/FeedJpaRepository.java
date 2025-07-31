package konkuk.thip.feed.adapter.out.persistence.repository;

import konkuk.thip.common.entity.StatusType;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedJpaRepository extends JpaRepository<FeedJpaEntity, Long>, FeedQueryRepository {

    @Query("SELECT COUNT(f) FROM FeedJpaEntity f WHERE f.userJpaEntity.userId = :userId AND f.status = :status")
    long countAllFeedsByUserId(@Param("userId") Long userId, @Param("status") StatusType status);

    @Query("SELECT COUNT(f) FROM FeedJpaEntity f WHERE f.userJpaEntity.userId = :userId AND f.isPublic = TRUE AND f.status = :status")
    long countPublicFeedsByUserId(@Param("userId") Long userId, @Param("status") StatusType status);
}
