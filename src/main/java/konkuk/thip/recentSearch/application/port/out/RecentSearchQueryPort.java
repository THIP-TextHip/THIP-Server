package konkuk.thip.recentSearch.application.port.out;

import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchType;
import konkuk.thip.recentSearch.domain.RecentSearch;

import java.util.List;
import java.util.Optional;

public interface RecentSearchQueryPort {

    Optional<RecentSearch> findRecentSearchByKeywordAndUserId(String keyword, Long userId, RecentSearchType type);

    List<RecentSearch> findRecentSearchesByTypeAndUserId(RecentSearchType type, Long userId, int limit);
}

