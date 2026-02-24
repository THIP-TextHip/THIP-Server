package konkuk.thip.feed.adapter.out.event.dto;

public record FeedCreatedEvent(Long feedId) {
    public static FeedCreatedEvent from(Long feedId) {
        return new FeedCreatedEvent(feedId);
    }
}