package konkuk.thip.saved.application.port.out;

import konkuk.thip.book.domain.SavedBooks;
import konkuk.thip.feed.domain.SavedFeeds;

import java.util.List;
import java.util.Set;

public interface SavedQueryPort {
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
    SavedBooks findSavedBooksByUserId(Long userId);
    SavedFeeds findSavedFeedsByUserId(Long userId);

    Set<Long> findSavedFeedIdsByUserIdAndFeedIds(Long userId, List<Long> feedIds);
}
