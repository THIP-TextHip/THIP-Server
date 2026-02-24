
package konkuk.thip.feed.adapter.out.event.dto;

public record FeedUpdatedEvent(Long feedId) {
    public static FeedUpdatedEvent from(Long feedId) {
        return new FeedUpdatedEvent(feedId);
    }
}