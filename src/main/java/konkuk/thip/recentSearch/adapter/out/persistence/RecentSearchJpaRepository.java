package konkuk.thip.recentSearch.adapter.out.persistence;

import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecentSearchJpaRepository extends JpaRepository<RecentSearchJpaEntity, Long> {
}
