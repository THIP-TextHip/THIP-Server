package konkuk.thip.recentSearch.adapter.out.persistence;

import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchType;
import konkuk.thip.recentSearch.adapter.out.mapper.RecentSearchMapper;
import konkuk.thip.recentSearch.adapter.out.persistence.repository.RecentSearchJpaRepository;
import konkuk.thip.recentSearch.application.port.out.RecentSearchQueryPort;
import konkuk.thip.recentSearch.domain.RecentSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RecentSearchQueryPersistenceAdapter implements RecentSearchQueryPort {

    private final RecentSearchJpaRepository recentSearchJpaRepository;
    private final RecentSearchMapper recentSearchMapper;

    @Override
    public Optional<RecentSearch> findRecentSearchByKeywordAndUserId(String keyword, Long userId, RecentSearchType type) {
        return recentSearchJpaRepository.findBySearchTermAndTypeAndUserId(keyword, type, userId)
                .map(recentSearchMapper::toDomainEntity);
    }

    @Override
    public List<RecentSearch> findRecentSearchesByTypeAndUserId(RecentSearchType type, Long userId, int limit) {
        return recentSearchJpaRepository.findByTypeAndUserId(type, userId, limit)
                .stream()
                .map(recentSearchMapper::toDomainEntity)
                .toList();
    }
}
