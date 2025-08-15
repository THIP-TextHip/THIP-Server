package konkuk.thip.feed.application.service;

import konkuk.thip.book.application.port.out.BookQueryPort;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.feed.adapter.in.web.response.FeedRelatedWithBookResponse;
import konkuk.thip.feed.application.mapper.FeedQueryMapper;
import konkuk.thip.feed.application.port.in.FeedRelatedWithBookUseCase;
import konkuk.thip.feed.application.port.in.dto.FeedRelatedWithBookQuery;
import konkuk.thip.feed.application.port.out.FeedQueryPort;
import konkuk.thip.feed.application.port.out.dto.FeedQueryDto;
import konkuk.thip.post.application.port.out.PostLikeQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedRelatedWithBookService implements FeedRelatedWithBookUseCase {

    private final FeedQueryPort feedQueryPort;
    private final BookQueryPort bookQueryPort;
    private final PostLikeQueryPort postLikeQueryPort;

    private final FeedQueryMapper feedQueryMapper;

    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    public FeedRelatedWithBookResponse getFeedsByBook(FeedRelatedWithBookQuery query) {

        // 책이 DB에 존재하는지 확인
        boolean isBookExist = bookQueryPort.existsBookByIsbn(query.isbn());

        // 책이 존재하지 않는 경우 빈 리스트로 응답
        if(!isBookExist) {
            return FeedRelatedWithBookResponse.of(List.of(), null, true);
        }

        Cursor cursor = Cursor.from(query.cursor(), DEFAULT_PAGE_SIZE);

        // 정렬 조건에 따른 피드 조회
        CursorBasedList<FeedQueryDto> feedQueryDtoCursorBasedList = switch(query.sortType()) {
            case LIKE -> feedQueryPort.findFeedsByBookIsbnOrderByLike(query.isbn(), query.userId(), cursor);
            case LATEST -> feedQueryPort.findFeedsByBookIsbnOrderByLatest(query.isbn(), query.userId(), cursor);
        };

        // 사용자가 저장한 피드 ID를 조회
        Set<Long> savedFeedIds = feedQueryPort.findSavedFeedIdsByUserIdAndFeedIds(
                feedQueryDtoCursorBasedList.contents().stream()
                        .map(FeedQueryDto::feedId)
                        .collect(Collectors.toSet()),
                query.userId()
        );

        // 사용자가 좋아요한 피드 ID를 조회
        Set<Long> likedFeedIds = postLikeQueryPort.findPostIdsLikedByUser(
                feedQueryDtoCursorBasedList.contents().stream()
                        .map(FeedQueryDto::feedId)
                        .collect(Collectors.toSet()),
                query.userId()
        );

        return FeedRelatedWithBookResponse.of(
                feedQueryMapper.toFeedRelatedWithBookDtos(
                        feedQueryDtoCursorBasedList.contents(),
                        savedFeedIds,
                        likedFeedIds,
                        query.userId()
                ),
                feedQueryDtoCursorBasedList.nextCursor(),
                feedQueryDtoCursorBasedList.isLast()
        );
    }
}
