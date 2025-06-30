package konkuk.thip.recentSearch.application.port.out;


import konkuk.thip.recentSearch.adapter.out.jpa.SearchType;

public interface RecentSearchCommandPort {
    void save(Long userId, String keyword, SearchType searchType);
}
