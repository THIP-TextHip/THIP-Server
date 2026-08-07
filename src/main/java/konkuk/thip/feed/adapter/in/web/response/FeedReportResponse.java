package konkuk.thip.feed.adapter.in.web.response;

import konkuk.thip.feed.application.port.in.dto.FeedReportResult;

public record FeedReportResponse(
        Long feedId,
        int reportCount
) {
        public static FeedReportResponse of(FeedReportResult feedReportResult) {
                return new FeedReportResponse(feedReportResult.feedId(), feedReportResult.reportCount());
        }
}
