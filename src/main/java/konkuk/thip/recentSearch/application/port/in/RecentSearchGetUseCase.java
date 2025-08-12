package konkuk.thip.recentSearch.application.port.in;

import konkuk.thip.recentSearch.adapter.in.web.response.RecentSearchGetResponse;

public interface RecentSearchGetUseCase {

    RecentSearchGetResponse getRecentSearches(String typeParam, Long userId);

}
