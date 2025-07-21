package konkuk.thip.feed.application.port.in.dto;

public record FeedIsSavedResult(
        Long feedId,
        boolean isSaved
)
{
    public static FeedIsSavedResult of(Long feedId, boolean isSaved) {
        return new FeedIsSavedResult(feedId, isSaved);
    }
}