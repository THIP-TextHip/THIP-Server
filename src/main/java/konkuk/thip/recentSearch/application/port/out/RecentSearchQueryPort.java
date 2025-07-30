package konkuk.thip.recentSearch.application.port.out;

import konkuk.thip.recentSearch.domain.RecentSearch;

import java.util.Optional;

public interface RecentSearchQueryPort {

    Optional<RecentSearch> findRecentSearchByKeywordAndUserId(String keyword, Long userId);

}
