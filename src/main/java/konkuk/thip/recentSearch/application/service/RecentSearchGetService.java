package konkuk.thip.recentSearch.application.service;

import konkuk.thip.recentSearch.adapter.in.web.response.RecentSearchGetResponse;
import konkuk.thip.recentSearch.application.RecentSearchQueryMapper;
import konkuk.thip.recentSearch.application.port.in.RecentSearchGetUseCase;
import konkuk.thip.recentSearch.application.port.out.RecentSearchQueryPort;
import konkuk.thip.recentSearch.domain.RecentSearch;
import konkuk.thip.recentSearch.domain.RecentSearchType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecentSearchGetService implements RecentSearchGetUseCase {

    private final RecentSearchQueryPort recentSearchQueryPort;

    private final RecentSearchQueryMapper recentSearchQueryMapper;

    @Transactional(readOnly = true)
    public RecentSearchGetResponse getRecentSearches(String typeParam, Long userId) {
        RecentSearchType recentSearchType = RecentSearchType.from(typeParam);
        List<RecentSearch> recentSearchList = recentSearchQueryPort.findRecentSearchesByTypeAndUserId(recentSearchType.getType(), userId);

        return RecentSearchGetResponse.of(
                recentSearchQueryMapper.toResponseList(recentSearchList)
        );
    }

}
