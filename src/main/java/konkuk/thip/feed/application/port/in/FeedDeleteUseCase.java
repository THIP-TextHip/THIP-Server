package konkuk.thip.feed.application.port.in;


public interface FeedDeleteUseCase {
    void deleteFeed(Long feedId, Long userId);
}
