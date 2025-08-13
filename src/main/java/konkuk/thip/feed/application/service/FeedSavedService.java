package konkuk.thip.feed.application.service;

import konkuk.thip.feed.application.port.in.FeedSavedUseCase;
import konkuk.thip.feed.application.port.in.dto.FeedIsSavedCommand;
import konkuk.thip.feed.application.port.in.dto.FeedIsSavedResult;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.feed.domain.SavedFeeds;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedSavedService implements FeedSavedUseCase {

    private final FeedCommandPort feedCommandPort;
    private final FeedQueryPort feedQueryPort;

    @Override
    @Transactional
    public FeedIsSavedResult changeSavedFeed(FeedIsSavedCommand feedIsSavedCommand) {

        // 1. 피드 검증 및 조회
        Feed feed = feedCommandPort.getByIdOrThrow(feedIsSavedCommand.feedId());

        // 2. 유저가 저장한 피드 목록 조회
        SavedFeeds savedFeeds = feedQueryPort.findSavedFeedsByUserId(feedIsSavedCommand.userId());

        if (feedIsSavedCommand.isSaved()) {
            // 저장 요청 시 이미 저장되어 있으면 예외 발생
            savedFeeds.validateNotAlreadySaved(feed);
            feedCommandPort.saveSavedFeed(feedIsSavedCommand.userId(), feed.getId());
        } else {
            // 삭제 요청 시 저장되어 있지 않으면 예외 발생
            savedFeeds.validateCanDelete(feed);
            feedCommandPort.deleteSavedFeed(feedIsSavedCommand.userId(), feed.getId());
        }

        return FeedIsSavedResult.of(feed.getId(), feedIsSavedCommand.isSaved());
    }
}
