package konkuk.thip.feed.application.port.in;

import konkuk.thip.feed.application.port.in.dto.FeedReportResult;

public interface FeedReportUseCase {
    FeedReportResult reportFeed(Long feedId);
}
