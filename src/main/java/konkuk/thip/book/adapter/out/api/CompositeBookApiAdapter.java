package konkuk.thip.book.adapter.out.api;

import konkuk.thip.book.adapter.out.api.aladin.AladinApiClient;
import konkuk.thip.book.adapter.out.api.aladin.AladinApiUtil;
import konkuk.thip.book.adapter.out.api.dto.BookSearchResult;
import konkuk.thip.book.adapter.out.api.dto.BookDetailResult;
import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.domain.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompositeBookApiAdapter implements BookApiQueryPort {

    private final AladinApiClient aladinApiClient;

    @Override
    public BookSearchResult findBooksByKeyword(String keyword, int start) {
        return aladinApiClient.findBooksByKeyword(keyword, start);
    }

    @Override
    public BookDetailResult 랑(String isbn) {
        return aladinApiClient.findDetailBookByIsbn(isbn);
    }

    @Override
    public Integer findPageCountByIsbn(String isbn) {
        return aladinApiClient.findPageCountByIsbn(isbn);
    }

    @Override
    public Book loadBookWithPageByIsbn(String isbn) {
        // 상세정보 + page 정보를 알라딘 ItemLookUp 한 번의 호출로 함께 조회
        AladinApiUtil.AladinDetailResult result = aladinApiClient.findDetailBookWithPageCountByIsbn(isbn);
        BookDetailResult detail = result.detail();

        return Book.withoutId(
                detail.title(),
                isbn,
                detail.author(),
                false,      // TODO : 추후 BestSeller 도입되면 고려해야함
                detail.publisher(),
                detail.imageUrl(),
                result.pageCount(),
                detail.description()
        );
    }
}
