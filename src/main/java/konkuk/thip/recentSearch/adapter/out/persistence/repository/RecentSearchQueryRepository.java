package konkuk.thip.recentSearch.adapter.out.persistence.repository;

import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchJpaEntity;
import konkuk.thip.recentSearch.domain.value.RecentSearchType;

import java.util.List;
import java.util.Optional;

public interface RecentSearchQueryRepository {
    Optional<RecentSearchJpaEntity> findBySearchTermAndTypeAndUserId(String searchTerm, RecentSearchType type, Long userId);

    List<RecentSearchJpaEntity> findByTypeAndUserId(RecentSearchType type, Long userId, int limit);
}
