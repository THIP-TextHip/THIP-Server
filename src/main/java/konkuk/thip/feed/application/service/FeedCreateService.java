package konkuk.thip.feed.application.service;

import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.feed.application.port.in.FeedCreateUseCase;
import konkuk.thip.feed.application.port.in.dto.FeedCreateCommand;
import konkuk.thip.feed.application.port.out.FeedCommandPort;
import konkuk.thip.feed.domain.Feed;
import konkuk.thip.feed.domain.value.Tag;
import konkuk.thip.feed.domain.value.TagList;
import konkuk.thip.feed.domain.value.ContentList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedCreateService implements FeedCreateUseCase {

    private final BookCommandPort bookCommandPort;
    private final FeedCommandPort feedCommandPort;
    private final BookApiQueryPort bookApiQueryPort;

    @Override
    @Transactional
    public Long createFeed(FeedCreateCommand command) {

        // 1. 피드 생성 비지니스 정책 검증
        TagList.validateTags(Tag.fromList(command.tagList()));
        ContentList.validateImageCount(ContentList.of(command.imageUrls()).size());

        // 2. Book 검증 및 조회
        Long targetBookId = findOrCreateBookByIsbn(command.isbn());

        // 3. Feed 생성 및 저장
        Feed feed = Feed.withoutId(
                command.contentBody(),
                command.userId(),
                command.isPublic(),
                targetBookId,
                command.tagList(),
                command.imageUrls()
        );
        return feedCommandPort.save(feed);
    }

    /**
     * ISBN으로 책을 조회하고, 없으면 외부 API(Naver)에서 상세 정보를 조회해 새로 저장 후 ID 반환
     */
    private Long findOrCreateBookByIsbn(String isbn) {
        return bookCommandPort.findByIsbn(isbn)
                .map(Book::getId)
                .orElseGet(() -> saveNewBookWithFromExternalApi(isbn));
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
