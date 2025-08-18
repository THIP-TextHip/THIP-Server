package konkuk.thip.feed.application.service;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.feed.adapter.in.web.response.FeedShowSavedListResponse;
import konkuk.thip.feed.application.mapper.FeedQueryMapper;
import konkuk.thip.feed.application.port.in.FeedSavedListUseCase;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import konkuk.thip.post.application.port.out.PostLikeQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedShowSavedListService implements FeedSavedListUseCase {

    private static final int PAGE_SIZE = 10;
    private final FeedQueryPort feedQueryPort;
    private final PostLikeQueryPort postLikeQueryPort;
    private final FeedQueryMapper feedQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public FeedShowSavedListResponse getSavedFeedList(Long userId, String cursor) {
        // 1. 커서 생성
        Cursor nextCursor = Cursor.from(cursor, PAGE_SIZE);

        // 2. 유저가 저장한 책 최신순으로 (페이징 처리 포함)
        CursorBasedList<FeedQueryDto> result = feedQueryPort.findSavedFeedsByCreatedAt(userId, nextCursor);
        Set<Long> feedIds = result.contents().stream()
                .map(FeedQueryDto::feedId)
                .collect(Collectors.toUnmodifiableSet());

        // 3. 유저가 좋아한 피드들 조회
        Set<Long> likedFeedIdsByUser = postLikeQueryPort.findPostIdsLikedByUser(feedIds, userId);

        // 4. response 로의 매핑
        List<FeedShowSavedListResponse.FeedShowSavedInfoDto> feedList = result.contents().stream()
                .map(dto -> feedQueryMapper.toFeedShowSavedListResponse(dto, likedFeedIdsByUser, userId))
                .toList();

        return new FeedShowSavedListResponse(
                feedList,
                result.nextCursor(),
                !result.hasNext()
        );
    }
}
