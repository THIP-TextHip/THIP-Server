package konkuk.thip.recentSearch.adapter.out.persistence.repository;

import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecentSearchJpaRepository extends JpaRepository<RecentSearchJpaEntity, Long>, RecentSearchQueryRepository {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RecentSearchJpaEntity r SET r.modifiedAt = CURRENT_TIMESTAMP WHERE r.recentSearchId = :recentSearchId")
    void updateModifiedAt(@Param("recentSearchId") Long recentSearchId);
}
