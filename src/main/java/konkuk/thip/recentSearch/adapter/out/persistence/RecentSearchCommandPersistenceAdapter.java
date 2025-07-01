package konkuk.thip.recentSearch.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.recentSearch.adapter.out.jpa.RecentSearchJpaEntity;
import konkuk.thip.recentSearch.adapter.out.jpa.SearchType;
import konkuk.thip.recentSearch.adapter.out.mapper.RecentSearchMapper;
import konkuk.thip.recentSearch.application.port.out.RecentSearchCommandPort;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static konkuk.thip.common.exception.code.ErrorCode.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class RecentSearchCommandPersistenceAdapter implements RecentSearchCommandPort {

    private final RecentSearchJpaRepository recentSearchJpaRepository;
    private final UserJpaRepository userJpaRepository;

    private final RecentSearchMapper recentSearchMapper;

    @Override
    public void save(Long userId, String keyword, SearchType searchType) {

        UserJpaEntity userJpaEntity = userJpaRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        RecentSearchJpaEntity entity = RecentSearchJpaEntity.builder()
                .searchTerm(keyword)
                .type(searchType)
                .userJpaEntity(userJpaEntity)
                .build();

        recentSearchJpaRepository.save(entity);
    }
}
