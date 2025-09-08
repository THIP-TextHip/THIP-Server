package konkuk.thip.feed.adapter.out.persistence.repository;

import konkuk.thip.feed.adapter.out.jpa.FeedJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedJpaRepository extends JpaRepository<FeedJpaEntity, Long>, FeedQueryRepository {

    /**
     * 소프트 딜리트 적용 대상 entity 단건 조회 메서드
     */
    Optional<FeedJpaEntity> findByPostId(Long postId);

    @Query("SELECT COUNT(f) FROM FeedJpaEntity f WHERE f.userJpaEntity.userId = :userId")
    long countAllFeedsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM FeedJpaEntity f WHERE f.userJpaEntity.userId = :userId AND f.isPublic = TRUE")
    long countPublicFeedsByUserId(@Param("userId") Long userId);

    @Query("SELECT f.postId FROM FeedJpaEntity f WHERE f.userJpaEntity.userId = :userId")
    List<Long> findFeedIdsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE FeedJpaEntity f SET f.status = 'INACTIVE' WHERE f.userJpaEntity.userId = :userId")
    void softDeleteAllByUserId(@Param("userId") Long userId);

}
