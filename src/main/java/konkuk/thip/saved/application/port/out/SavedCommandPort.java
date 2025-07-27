package konkuk.thip.saved.application.port.out;


public interface SavedCommandPort {
    void saveBook(Long userId, Long bookId);
    void deleteBook(Long userId, Long bookId);
    void saveFeed(Long userId, Long feedId);
    void deleteFeed(Long userId, Long feedId);
}
