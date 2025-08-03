package konkuk.thip.feed.application.service;

import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.feed.adapter.in.web.response.FeedShowSingleResponse;
import konkuk.thip.feed.application.mapper.FeedQueryMapper;
import konkuk.thip.feed.application.port.in.FeedShowSingleUseCase;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.post.application.port.out.PostLikeQueryPort;
import konkuk.thip.saved.application.port.out.SavedQueryPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

import static konkuk.thip.common.exception.code.ErrorCode.FEED_CAN_NOT_SHOW_PRIVATE_ONE;

@Service
@RequiredArgsConstructor
public class FeedShowSingleService implements FeedShowSingleUseCase {

    private final FeedCommandPort feedCommandPort;
    private final UserCommandPort userCommandPort;
    private final BookCommandPort bookCommandPort;
    private final PostLikeQueryPort postLikeQueryPort;
    private final SavedQueryPort savedQueryPort;
    private final FeedQueryMapper feedQueryMapper;

    @Override
    public FeedShowSingleResponse showSingleFeed(Long feedId, Long userId) {
        // 1. 단일 피드 조회 및 피드 조회 유효성 검증
        Feed feed = feedCommandPort.getByIdOrThrow(feedId);
        feed.validateViewPermission(userId);

        // 2. 피드 작성자 도메인 조회
        User feedCreator = userCommandPort.findById(feed.getCreatorId());

        // 3. book 도메인 조회
        Book book = bookCommandPort.findById(feed.getTargetBookId());

        // 4. 유저가 해당 피드를 좋아하는지, 저장했는지 여부 조회
        Set<Long> savedFeedIds = savedQueryPort.findSavedFeedIdsByUserIdAndFeedIds(Set.of(feedId), userId);
        Set<Long> likedFeedIds = postLikeQueryPort.findPostIdsLikedByUser(Set.of(feedId), userId);
        boolean isSaved = savedFeedIds.contains(feedId);
        boolean isLiked = likedFeedIds.contains(feedId);

        return feedQueryMapper.toFeedShowSingleResponse(feed, feedCreator, book, isSaved, isLiked);
    }
}
