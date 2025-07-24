package konkuk.thip.feed.application.service;

import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.common.util.DateUtil;
import konkuk.thip.feed.adapter.in.web.response.FeedShowAllResponse;
import konkuk.thip.feed.application.mapper.FeedQueryMapper;
import konkuk.thip.feed.application.port.in.FeedShowAllUseCase;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    private final FeedQueryMapper feedQueryMapper;

    @Transactional(readOnly = true)
    @Override
    public FeedShowAllResponse showAllFeeds(Long userId, String cursor) {
        // 1. 커서 파싱 : createdAt을 커서로 사용한다
        LocalDateTime cursorVal = cursor != null && !cursor.isBlank() ? DateUtil.parseDateTime(cursor) : null;

        // 2. [최신순으로] 피드 조회 with 페이징 처리
        CursorBasedList<FeedQueryDto> result = feedQueryPort.findLatestFeedsByCreatedAt(userId, cursorVal, PAGE_SIZE);

        // 3. response 로의 매핑
        List<FeedShowAllResponse.Feed> feedList = result.contents().stream()
                .map(feedQueryMapper::toFeedShowAllResponse)
                .toList();

        return new FeedShowAllResponse(
                feedList,
                result.nextCursor(),
                !result.hasNext()
        );
    }
}
