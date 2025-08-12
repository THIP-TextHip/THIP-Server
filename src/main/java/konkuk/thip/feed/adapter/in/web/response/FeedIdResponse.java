package konkuk.thip.feed.adapter.in.web.response;

public record FeedIdResponse(Long feedId) {
    public static FeedIdResponse of(Long feedId) {
        return new FeedIdResponse(feedId);
    }
}
