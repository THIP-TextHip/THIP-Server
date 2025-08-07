package konkuk.thip.saved.application.port.out;

import konkuk.thip.feed.domain.SavedFeeds;

import java.util.Set;

public interface SavedQueryPort {

    SavedFeeds findSavedFeedsByUserId(Long userId);

    Set<Long> findSavedFeedIdsByUserIdAndFeedIds(Set<Long> feedIds, Long userId);
}
