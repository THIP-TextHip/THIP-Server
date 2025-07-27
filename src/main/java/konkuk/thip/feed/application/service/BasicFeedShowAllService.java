package konkuk.thip.feed.application.service;

import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.feed.adapter.in.web.response.FeedShowAllResponse;
import konkuk.thip.feed.application.mapper.FeedQueryMapper;
import konkuk.thip.feed.application.port.in.FeedShowAllUseCase;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import konkuk.thip.post.application.port.out.PostLikeQueryPort;
import konkuk.thip.saved.application.port.out.SavedQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@ConditionalOnProperty(
        name = "feed.show.strategy",
        havingValue = "basic",
        matchIfMissing = true       // 프로퍼티가 없거나 basic 이면 이 구현체 사용
)
@RequiredArgsConstructor
public class BasicFeedShowAllService implements FeedShowAllUseCase {

    /**
     * 해당 서비스는 최신순 정렬하는 Service 구현체
     */

    private static final int PAGE_SIZE = 10;
    private final FeedQueryPort feedQueryPort;
    private final SavedQueryPort savedQueryPort;
    private final PostLikeQueryPort postLikeQueryPort;
    private final FeedQueryMapper feedQueryMapper;

    @Transactional(readOnly = true)
    @Override
    public FeedShowAllResponse showAllFeeds(Long userId, String cursor) {
        // 1. 커서 파싱 : createdAt을 커서로 사용한다
        LocalDateTime cursorVal = cursor != null && !cursor.isBlank() ? DateUtil.parseDateTime(cursor) : null;

        // 2. [최신순으로] 피드 조회 with 페이징 처리
        CursorBasedList<FeedQueryDto> result = feedQueryPort.findLatestFeedsByCreatedAt(userId, cursorVal, PAGE_SIZE);
        List<Long> feedIds = result.contents().stream()
                .map(FeedQueryDto::feedId)
                .toList();

        // 3. 유저가 저장한 피드들, 좋아한 피드들 조회
        Set<Long> savedFeedIdsByUser = savedQueryPort.findSavedFeedIdsByUserIdAndFeedIds(userId, feedIds);
        Set<Long> likedFeedIdsByUser = postLikeQueryPort.findLikedFeedIdsByUserIdAndFeedIds(userId, feedIds);

        // 4. response 로의 매핑
        List<FeedShowAllResponse.FeedDto> feedList = result.contents().stream()
                .map(dto -> feedQueryMapper.toFeedShowAllResponse(dto, savedFeedIdsByUser, likedFeedIdsByUser))
                .toList();

        return new FeedShowAllResponse(
                feedList,
                result.nextCursor(),
                !result.hasNext()
        );
    }
}
