package konkuk.thip.recentSearch.application.port.out;


import konkuk.thip.recentSearch.domain.RecentSearch;

public interface RecentSearchCommandPort {
    void save(RecentSearch recentSearch);
    void delete(Long id);

    void update(RecentSearch recentSearch);
}
