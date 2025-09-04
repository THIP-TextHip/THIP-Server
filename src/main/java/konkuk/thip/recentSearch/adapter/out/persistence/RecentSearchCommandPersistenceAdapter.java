package konkuk.thip.recentSearch.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchJpaEntity;
import konkuk.thip.recentSearch.adapter.out.mapper.RecentSearchMapper;
import konkuk.thip.recentSearch.adapter.out.persistence.repository.RecentSearchJpaRepository;
import konkuk.thip.recentSearch.application.port.out.RecentSearchCommandPort;
import konkuk.thip.recentSearch.domain.RecentSearch;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.RECENT_SEARCH_NOT_FOUND;
import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class RecentSearchCommandPersistenceAdapter implements RecentSearchCommandPort {

    private final RecentSearchJpaRepository recentSearchJpaRepository;
    private final UserJpaRepository userJpaRepository;

    private final RecentSearchMapper recentSearchMapper;

    @Override
    public Optional<RecentSearch> findById(Long id) {
        return recentSearchJpaRepository.findById(id)
                .map(recentSearchMapper::toDomainEntity);
    }

    @Override
    public void save(RecentSearch recentSearch) {

        UserJpaEntity userJpaEntity = userJpaRepository.findById(recentSearch.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        RecentSearchJpaEntity recentSearchJpaEntity =
                recentSearchMapper.toJpaEntity(recentSearch, userJpaEntity);

        recentSearchJpaRepository.save(recentSearchJpaEntity);
    }

    @Override
    public void delete(Long id) {
        recentSearchJpaRepository.delete(
                recentSearchJpaRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException(RECENT_SEARCH_NOT_FOUND))
        );
    }

    @Override
    public void touch(RecentSearch recentSearch) {
        recentSearchJpaRepository.updateModifiedAt(recentSearch.getId());
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        recentSearchJpaRepository.deleteAllByUserId(userId);
    }


}
