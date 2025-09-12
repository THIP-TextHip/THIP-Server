package konkuk.thip.recentSearch.application.port.out;


import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.recentSearch.domain.RecentSearch;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.RECENT_SEARCH_NOT_FOUND;

public interface RecentSearchCommandPort {

    Optional<RecentSearch> findById(Long id);

    default RecentSearch getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RECENT_SEARCH_NOT_FOUND));
    }

    void save(RecentSearch recentSearch);
    void delete(Long id);

    void touch(RecentSearch recentSearch); // modifiedAt 갱신용 메서드

    void deleteAllByUserId(Long userId);
}
