package konkuk.thip.recentSearch.adapter.out.persistence.repository;

import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchJpaEntity;
import konkuk.thip.recentSearch.adapter.out.jpa.SearchType;

import java.util.List;
import java.util.Optional;

public interface RecentSearchQueryRepository {
    Optional<RecentSearchJpaEntity> findBySearchTermAndTypeAndUserId(String searchTerm, SearchType type, Long userId);

    List<RecentSearchJpaEntity> findByTypeAndUserId(String type, Long userId);
}
