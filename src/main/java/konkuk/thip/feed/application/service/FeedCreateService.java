package konkuk.thip.feed.application.service;

import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.feed.application.port.in.FeedCreateUseCase;
import konkuk.thip.feed.application.port.in.dto.FeedCreateCommand;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.feed.application.port.out.S3CommandPort;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedCreateService implements FeedCreateUseCase {

    private final S3CommandPort s3CommandPort;
    private final RoomCommandPort roomCommandPort;
    private final BookCommandPort bookCommandPort;
    private final FeedCommandPort feedCommandPort;
    private final BookApiQueryPort bookApiQueryPort;

    @Override
    @Transactional
    public Long createFeed(FeedCreateCommand command, List<MultipartFile> images) {

        // 1. 피드 생성 비지니스 정책 검증
        Feed.validateCategoryAndTags(command.category(), command.tagList());
        Feed.validateImageCount(images);

        // 2. Category 검증 및 조회
        validateCategoryAndTagList(command.category(), command.tagList());

        // 3. Book 검증 및 조회
        Long targetBookId = findOrCreateBookByIsbn(command.isbn());

        // 4. 이미지 업로드
        List<String> imageUrls = (images == null || images.isEmpty())
                ? List.of()
                : s3CommandPort.uploadImages(images);

        // 5. Feed 생성 및 저장 (Content도 함께 생성 및 저장 애그리거트 루트인 Feed가 생성책임 가지고있음)
        try {
            Feed feed = Feed.withoutId(
                    command.contentBody(),
                    command.userId(),
                    command.isPublic(),
                    targetBookId,
                    command.tagList(),
                    imageUrls
            );
            return feedCommandPort.save(feed);

        } catch (Exception e) {
            if (imageUrls != null) {
                s3CommandPort.deleteImages(imageUrls);
            }
            throw e;
        }
    }

    // TODO: 카테고리, 태그 관계가 명확해지면 카테고리 내의 도메인에서 검증하도록 리팩토링 예정
    private void validateCategoryAndTagList(String categoryValue, List<String> tagList) {

        boolean hasCategoryAndTags = categoryValue != null && !categoryValue.trim().isEmpty()
                && tagList != null && !tagList.isEmpty();

        // Category 검증 및 조회
        if(hasCategoryAndTags) { roomCommandPort.findCategoryByValue(categoryValue); }

        // TODO: Category로 tagList 검증
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
