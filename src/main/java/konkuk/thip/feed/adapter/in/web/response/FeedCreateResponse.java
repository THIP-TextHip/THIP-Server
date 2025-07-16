package konkuk.thip.feed.adapter.in.web.response;

public record FeedCreateResponse(Long feedId) {
    public static FeedCreateResponse of(Long feedId) {
        return new FeedCreateResponse(feedId);
    }
}
