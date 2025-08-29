package konkuk.thip.recentSearch.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.recentSearch.application.port.in.RecentSearchDeleteUseCase;
import konkuk.thip.recentSearch.application.port.out.RecentSearchCommandPort;
import konkuk.thip.recentSearch.domain.RecentSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecentSearchDeleteService implements RecentSearchDeleteUseCase {

    private final RecentSearchCommandPort recentSearchCommandPort;

    @Override
    @Transactional
    public Void deleteRecentSearch(Long recentSearchId, Long userId) {
        RecentSearch recentSearch = recentSearchCommandPort.getByIdOrThrow(recentSearchId);
        if (!recentSearch.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.RECENT_SEARCH_NOT_ADDED_BY_USER);
        }

        recentSearchCommandPort.delete(recentSearch.getId());
        return null;
    }
}
