package konkuk.thip.feed.application.service;

import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.feed.application.port.in.FeedCreateUseCase;
import konkuk.thip.feed.application.port.in.dto.FeedCreateCommand;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.feed.domain.Tag;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.API_INVALID_PARAM;

@Service
@RequiredArgsConstructor
public class FeedCreateService implements FeedCreateUseCase {

    private final RoomCommandPort roomCommandPort;
    private final BookCommandPort bookCommandPort;
    private final FeedCommandPort feedCommandPort;
    private final BookApiQueryPort bookApiQueryPort;


    @Override
    @Transactional
    public Long createFeed(FeedCreateCommand command) {

        // 요청 값 검증
        validateCategoryAndTags(command.category(), command.tagList());

        // Category/Tag 생성 및 조회 함수 사용
        List<Tag> tags = resolveTags(command.category(), command.tagList());

        // 2. Book 찾기, 없으면 Book 로드 및 저장
        Long targetBookId = findOrCreateBookByIsbn(command.isbn());

        // Feed 생성 및 저장 ( Content도 함께 생성 및 저장 애그리거트 루트인 Feed가 생성책임 가지고있음)
        Feed feed = Feed.withoutId(
                command.contentBody(),
                command.userId(),
                command.isPublic(),
                targetBookId,
                tags,
                command.imageUrls()
        );

        return feedCommandPort.save(feed);
    }

    private void validateCategoryAndTags(String category, List<String> tagList) {

        // 둘 다 없으면 카테고리도 태그도 없는 새 게시글 (예외 상황 아님)
        boolean categoryEmpty = (category == null || category.trim().isEmpty());
        boolean tagListEmpty = (tagList == null || tagList.isEmpty());

        // 둘 중 하나만 입력된 경우
        if (categoryEmpty ^ tagListEmpty) {
            throw new InvalidStateException(API_INVALID_PARAM, new IllegalArgumentException("카테고리와 태그는 모두 입력되거나 모두 비워져야 합니다."));
        }

        // 태그가 있는 경우, 개수 최대 5개 제한
        if (!tagListEmpty && tagList.size() > 5) {
            throw new InvalidStateException(API_INVALID_PARAM, new IllegalArgumentException( "태그는 최대 5개까지 입력할 수 있습니다."));
        }

        // 태그 중복 체크
        if (!tagListEmpty) {
            long distinctCount = tagList.stream().distinct().count();
            if (distinctCount != tagList.size()) {
                throw new InvalidStateException(API_INVALID_PARAM, new IllegalArgumentException("태그에 중복된 값이 있습니다."));
            }
        }
    }

    private List<Tag> resolveTags(String categoryValue, List<String> tagList) {

        boolean hasCategoryAndTags = categoryValue != null && !categoryValue.trim().isEmpty()
                && tagList != null && !tagList.isEmpty();

        if (!hasCategoryAndTags) {
            return List.of(); // 빈 태그 리스트 반환
        }

        // Category 검증 및 조회
        Category category = roomCommandPort.findCategoryByValue(categoryValue);

        // TODO: Category로 tagList 검증

        // Tag 생성 및 반환
        return Tag.fromList(tagList);
    }



    /**
     * ISBN으로 책을 조회하고, 없으면 외부 API(Naver)에서 상세 정보를 조회해 새로 저장 후 ID 반환
     */
    private Long findOrCreateBookByIsbn(String isbn) {
        try {
            Book existing = bookCommandPort.findByIsbn(isbn);
            return existing.getId();
        } catch (EntityNotFoundException e) {
            return saveNewBookWithFromExternalApi(isbn);
        }
    }

    /**
     * 외부 API(Naver)를 통해 상세 책 정보를 조회하고 Book 도메인으로 저장
     */
    private Long saveNewBookWithFromExternalApi(String isbn) {
        NaverDetailBookParseResult detailBookByKeyword = bookApiQueryPort.findDetailBookByIsbn(isbn);
        Book newBook = Book.withoutId(
                detailBookByKeyword.title(),
                detailBookByKeyword.isbn(),
                detailBookByKeyword.author(),
                false,  // TODO : 추후 BestSeller 도입 시 로직 수정
                detailBookByKeyword.publisher(),
                detailBookByKeyword.imageUrl(),
                null,
                detailBookByKeyword.description());
        return bookCommandPort.save(newBook);
    }
}
