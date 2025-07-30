package konkuk.thip.recentSearch.application.port.in;

import java.util.List;

public interface RecentSearchGetUseCase {

    List<String> getRecentSearches(String typeParam, Long userId);

}
