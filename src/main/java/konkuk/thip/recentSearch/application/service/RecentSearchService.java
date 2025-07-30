package konkuk.thip.recentSearch.application.service;

import konkuk.thip.recentSearch.application.port.in.RecentSearchGetUseCase;
import konkuk.thip.recentSearch.application.port.out.RecentSearchQueryPort;
import konkuk.thip.recentSearch.domain.RecentSearch;
import konkuk.thip.recentSearch.domain.RecentSearchType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecentSearchService implements RecentSearchGetUseCase {

    private final RecentSearchQueryPort recentSearchQueryPort;

    public List<String> getRecentSearches(String typeParam, Long userId) {
        RecentSearchType recentSearchType = RecentSearchType.from(typeParam);
        List<RecentSearch> recentSearchList = recentSearchQueryPort.findRecentSearchesByTypeAndUserId(recentSearchType.getType(), userId);

        return recentSearchList.stream()
                .map(RecentSearch::getSearchTerm)
                .toList();
    }

}
