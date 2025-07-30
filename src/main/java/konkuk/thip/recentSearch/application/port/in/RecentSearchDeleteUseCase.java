package konkuk.thip.recentSearch.application.port.in;

public interface RecentSearchDeleteUseCase {
    Void deleteRecentSearch(Long recentSearchId, Long userId);
}
