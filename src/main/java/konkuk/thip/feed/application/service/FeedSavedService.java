package konkuk.thip.feed.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.feed.application.port.in.FeedSavedUseCase;
import konkuk.thip.feed.application.port.in.dto.FeedIsSavedCommand;
import konkuk.thip.feed.application.port.in.dto.FeedIsSavedResult;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.feed.domain.Feed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class FeedSavedService implements FeedSavedUseCase {

    private final FeedCommandPort feedCommandPort;
    private final FeedQueryPort feedQueryPort;

    @Override
    @Transactional
    public FeedIsSavedResult changeSavedFeed(FeedIsSavedCommand command) {

        // 1. 피드 검증 및 조회
        Feed feed = feedCommandPort.getByIdOrThrow(command.feedId());

        // 2. 유저가 해당 피드를 저장했는지 여부 조회
        boolean alreadySaved = feedQueryPort.existsSavedFeedByUserIdAndFeedId(command.userId(), feed.getId());
        validateSaveFeedAction(command.isSaved(), alreadySaved);

        if (command.isSaved()) {
            feedCommandPort.saveSavedFeed(command.userId(), feed.getId());
        } else {
            feedCommandPort.deleteSavedFeed(command.userId(), feed.getId());
        }

        return FeedIsSavedResult.of(feed.getId(), command.isSaved());
    }

    private void validateSaveFeedAction(boolean isSaveRequest, boolean alreadySaved) {
        if (isSaveRequest && alreadySaved) {
            // 이미 저장되어 있는 피드를 다시 저장하려는 경우 예외 처리
            throw new BusinessException(FEED_ALREADY_SAVED);
        } else if (!isSaveRequest && !alreadySaved) {
            // 저장되지 않은 피드를 삭제하려는 경우 예외 처리
            throw new BusinessException(FEED_NOT_SAVED_CANNOT_DELETE);
        }
    }
}
