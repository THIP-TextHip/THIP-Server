package konkuk.thip.feed.adapter.out.event.dto;

public record FeedDeletedEvent(Long feedId) {
    public static FeedDeletedEvent from(Long feedId) {
        return new FeedDeletedEvent(feedId);
    }
}