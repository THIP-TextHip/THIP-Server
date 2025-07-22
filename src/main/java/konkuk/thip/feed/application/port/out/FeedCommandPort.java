package konkuk.thip.feed.application.port.out;


import konkuk.thip.feed.domain.Feed;

public interface FeedCommandPort {
    Long save(Feed feed);
    Long update(Feed feed);
    Feed findById(Long id);
}
