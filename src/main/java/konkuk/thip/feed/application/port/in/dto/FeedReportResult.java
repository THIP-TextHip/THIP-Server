package konkuk.thip.feed.application.port.in.dto;

public record FeedReportResult(
        Long feedId,
        int reportCount
) {
    public static FeedReportResult of(Long feedId, int reportCount) {
        return new FeedReportResult(feedId, reportCount);
    }
}
