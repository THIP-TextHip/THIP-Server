package konkuk.thip.recentSearch.application.port.out;


import konkuk.thip.recentSearch.domain.RecentSearch;

public interface RecentSearchCommandPort {
    void save(Long userId, RecentSearch recentSearch);
}
