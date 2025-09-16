package konkuk.thip.recentSearch.application.service.manager;

import konkuk.thip.common.annotation.application.HelperService;
import konkuk.thip.recentSearch.domain.value.RecentSearchType;
import konkuk.thip.recentSearch.application.port.out.RecentSearchCommandPort;
import konkuk.thip.recentSearch.application.port.out.RecentSearchQueryPort;
import konkuk.thip.recentSearch.domain.RecentSearch;
import lombok.RequiredArgsConstructor;

@HelperService
@RequiredArgsConstructor
public class RecentSearchCreateManager {

    private final RecentSearchCommandPort recentSearchCommandPort;
    private final RecentSearchQueryPort recentSearchQueryPort;

    public void saveRecentSearchByUser(Long userId, String keyword, RecentSearchType type, boolean isFinalized) {
        if (!isFinalized) return; // 검색완료일 경우에 최근검색어 추가
        if (keyword == null || keyword.trim().isEmpty()) return;
        String normalized = keyword.trim();

        // 동일 조건 (userId + keyword + type) 검색 기록이 이미 있는지 확인
        recentSearchQueryPort.findRecentSearchByKeywordAndUserId(normalized, userId, type)
                .ifPresentOrElse(
                        recentSearchCommandPort::touch, // 있으면 modifiedAt 갱신
                        () -> recentSearchCommandPort.save(RecentSearch.withoutId(normalized, type, userId)) // 없으면 새로 저장
                );
    }
}
