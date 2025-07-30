package konkuk.thip.feed.application.service;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.feed.adapter.in.web.response.FeedShowMineResponse;
import konkuk.thip.feed.application.mapper.FeedQueryMapper;
import konkuk.thip.feed.application.port.in.FeedShowMineUseCase;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedShowMineService implements FeedShowMineUseCase {

    private static final int PAGE_SIZE = 10;
    private final FeedQueryPort feedQueryPort;
    private final FeedQueryMapper feedQueryMapper;

    @Transactional(readOnly = true)
    @Override
    public FeedShowMineResponse showMyFeeds(Long userId, String cursor) {
        // 1. 커서 생성
        Cursor nextCursor = Cursor.from(cursor, PAGE_SIZE);

        // 2. [최신순으로] 피드 조회 with 페이징 처리
        CursorBasedList<FeedQueryDto> result = feedQueryPort.findMyFeedsByCreatedAt(userId, nextCursor);

        // 3. dto -> response 변환
        var feedList = result.contents().stream()
                .map(feedQueryMapper::toFeedShowMineResponse)
                .toList();

        return new FeedShowMineResponse(
                feedList,
                result.nextCursor(),
                !result.hasNext()
        );
    }
}
