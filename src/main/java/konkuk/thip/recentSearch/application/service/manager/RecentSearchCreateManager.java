package konkuk.thip.recentSearch.application.service.manager;

import konkuk.thip.recentSearch.application.port.out.RecentSearchCommandPort;
import konkuk.thip.recentSearch.application.port.out.RecentSearchQueryPort;
import konkuk.thip.recentSearch.domain.RecentSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RecentSearchCreateManager {

    private static final String USER_SEARCH_TERM = "사용자 검색";

    private final RecentSearchCommandPort recentSearchCommandPort;
    private final RecentSearchQueryPort recentSearchQueryPort;

    public void saveRecentSearchByUser(Long userId, String keyword) {

        // 동일 조건 (userId + keyword + type) 검색 기록이 이미 있는지 확인
        recentSearchQueryPort.findRecentSearchByKeywordAndUserId(keyword, userId)
                .ifPresentOrElse(
                        existingRecentSearch -> {
                            // 이미 존재하면 createdAt만 갱신
                            existingRecentSearch.updateModifiedAt(LocalDateTime.now());
                            recentSearchCommandPort.update(existingRecentSearch);
                        },
                        () -> {
                            // 없으면 새로 저장
                            RecentSearch userRecentSearch = RecentSearch.withoutId(keyword, USER_SEARCH_TERM, userId);
                            recentSearchCommandPort.save(userRecentSearch);
                        }
                );
    }
}
