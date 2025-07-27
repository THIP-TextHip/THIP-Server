package konkuk.thip.saved.application.port.out;

import konkuk.thip.book.domain.SavedBooks;
import konkuk.thip.feed.domain.SavedFeeds;

public interface SavedQueryPort {
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
    SavedBooks findSavedBooksByUserId(Long userId);
    SavedFeeds findSavedFeedsByUserId(Long userId);
}
