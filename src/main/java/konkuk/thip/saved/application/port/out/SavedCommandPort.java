package konkuk.thip.saved.application.port.out;


public interface SavedCommandPort {
    void saveFeed(Long userId, Long feedId);
    void deleteFeed(Long userId, Long feedId);
}
