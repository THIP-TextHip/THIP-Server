package konkuk.thip.feed.adapter.out.persistence.repository;

import konkuk.thip.common.entity.StatusType;
import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FeedJpaRepository extends JpaRepository<FeedJpaEntity, Long>, FeedQueryRepository {

    /**
     * 소프트 딜리트 적용 대상 entity 단건 조회 메서드
     */
    Optional<FeedJpaEntity> findByPostId(Long postId);

    @Query("SELECT COUNT(f) FROM FeedJpaEntity f WHERE f.userJpaEntity.userId = :userId AND f.status = :status")
    long countAllFeedsByUserId(@Param("userId") Long userId, @Param("status") StatusType status);

    @Query("SELECT COUNT(f) FROM FeedJpaEntity f WHERE f.userJpaEntity.userId = :userId AND f.isPublic = TRUE AND f.status = :status")
    long countPublicFeedsByUserId(@Param("userId") Long userId, @Param("status") StatusType status);
}
