package konkuk.thip.feed.application.service;

import konkuk.thip.feed.application.port.in.FeedReportUseCase;
import konkuk.thip.feed.application.port.in.dto.FeedReportResult;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.feed.domain.Feed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedReportService implements FeedReportUseCase {

    private final FeedCommandPort feedCommandPort;

    @Override
    @Transactional
    public FeedReportResult reportFeed(Long feedId) {
        Feed feed = feedCommandPort.getByIdOrThrow(feedId);
        feed.increaseReportCount();
        feedCommandPort.update(feed);

        return FeedReportResult.of(feed.getId(), feed.getReportCount());
    }
}
