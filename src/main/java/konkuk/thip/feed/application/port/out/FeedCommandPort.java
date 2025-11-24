package konkuk.thip.feed.application.port.out;


import java.util.List;
import java.util.Map;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.feed.domain.Feed;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.FEED_NOT_FOUND;

public interface FeedCommandPort {
    Long save(Feed feed);
    Long update(Feed feed);
    Optional<Feed> findById(Long id);
    List<Long> findByIds(List<Long> ids);
    default Feed getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException(FEED_NOT_FOUND));
    }
    void delete(Feed feed);
    void saveSavedFeed(Long userId, Long feedId);
    void deleteSavedFeed(Long userId, Long feedId);
    void deleteAllSavedFeedByUserId(Long userId);
    void deleteAllFeedByUserId(Long userId);
    void batchUpdateLikeCounts(Map<Long, Integer> idToLikeCount);
}
