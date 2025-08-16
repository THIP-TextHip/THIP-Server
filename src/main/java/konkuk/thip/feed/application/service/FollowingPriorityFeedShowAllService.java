package konkuk.thip.feed.application.service;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.feed.adapter.in.web.response.FeedShowAllResponse;
import konkuk.thip.feed.application.mapper.FeedQueryMapper;
import konkuk.thip.feed.application.port.in.FeedShowAllUseCase;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import konkuk.thip.post.application.port.out.PostLikeQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(
        name = "feed.show.strategy",
        havingValue = "following_priority"        // 프로퍼티가 following_priority 이면 이 구현체 사용
)
@RequiredArgsConstructor
public class FollowingPriorityFeedShowAllService implements FeedShowAllUseCase {

    /**
     * 팔로우하는 유저의 피드를 우선적으로 최신순 정렬하는 Service 구현체
     */

    private static final int PAGE_SIZE = 10;
    private final FeedQueryPort feedQueryPort;
    private final PostLikeQueryPort postLikeQueryPort;
    private final FeedQueryMapper feedQueryMapper;

    @Transactional(readOnly = true)
    @Override
    public FeedShowAllResponse showAllFeeds(Long userId, String cursor) {
        // 1. 커서 생성
        Cursor nextCursor = Cursor.from(cursor, PAGE_SIZE);

        // 2. [팔로우 하는 유저의 피드를 우선적으로] 피드 조회 with 페이징 처리
        CursorBasedList<FeedQueryDto> result = feedQueryPort.findFeedsByFollowingPriority(userId, nextCursor);
        Set<Long> feedIds = result.contents().stream()
                .map(FeedQueryDto::feedId)
                .collect(Collectors.toUnmodifiableSet());

        // 3. 유저가 저장한 피드들, 좋아한 피드들 조회
        Set<Long> savedFeedIdsByUser = feedQueryPort.findSavedFeedIdsByUserIdAndFeedIds(feedIds, userId);
        Set<Long> likedFeedIdsByUser = postLikeQueryPort.findPostIdsLikedByUser(feedIds, userId);

        // 4. response 로의 매핑
        List<FeedShowAllResponse.FeedShowAllDto> feedList = result.contents().stream()
                .map(dto -> feedQueryMapper.toFeedShowAllResponse(dto, savedFeedIdsByUser, likedFeedIdsByUser, userId))
                .toList();

        return new FeedShowAllResponse(
                feedList,
                result.nextCursor(),
                !result.hasNext()
        );
    }
}
