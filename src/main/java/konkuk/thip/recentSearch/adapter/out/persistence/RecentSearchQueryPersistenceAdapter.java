package konkuk.thip.recentSearch.adapter.out.persistence;

import konkuk.thip.recentSearch.adapter.out.mapper.RecentSearchMapper;
import konkuk.thip.recentSearch.application.port.out.RecentSearchQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecentSearchQueryPersistenceAdapter implements RecentSearchQueryPort {

    private final RecentSearchJpaRepository recentSearchJpaRepository;
    private final RecentSearchMapper recentSearchMapper;

}
