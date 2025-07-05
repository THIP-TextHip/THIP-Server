package konkuk.thip.saved.application.port.out;

import konkuk.thip.book.domain.SavedBooks;

public interface SavedQueryPort {
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
    SavedBooks findByUserId(Long userId);
}
