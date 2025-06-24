package konkuk.thip.recentSearch.adapter.out.persistence;

import konkuk.thip.recentSearch.adapter.out.mapper.RecentSearchMapper;
import konkuk.thip.recentSearch.application.port.out.RecentSearchCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecentSearchCommandPersistenceAdapter implements RecentSearchCommandPort {

    private final RecentSearchJpaRepository recentSearchJpaRepository;
    private final RecentSearchMapper recentSearchMapper;

}
