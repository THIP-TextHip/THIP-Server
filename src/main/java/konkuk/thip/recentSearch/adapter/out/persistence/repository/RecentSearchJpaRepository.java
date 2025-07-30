package konkuk.thip.recentSearch.adapter.out.persistence.repository;

import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecentSearchJpaRepository extends JpaRepository<RecentSearchJpaEntity, Long>, RecentSearchQueryRepository {
}
