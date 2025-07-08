package konkuk.thip.book.adapter.out.api;

import konkuk.thip.book.adapter.out.api.aladin.AladinApiClient;
import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.application.port.out.BookApiQueryPort;
import konkuk.thip.book.domain.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompositeBookApiAdapter implements BookApiQueryPort {

    private final NaverApiClient naverApiClient;
    private final AladinApiClient aladinApiClient;

    @Override
    public NaverBookParseResult findBooksByKeyword(String keyword, int start) {
        return naverApiClient.findBooksByKeyword(keyword, start);
    }

    @Override
    public NaverDetailBookParseResult findDetailBookByIsbn(String isbn) {
        return naverApiClient.findDetailBookByIsbn(isbn);
    }

    @Override
    public Integer findPageCountByIsbn(String isbn) {
        return aladinApiClient.findPageCountByIsb(isbn);
    }

    @Override
    public Book loadBookWithPageByIsbn(String isbn) {
        // 1. naver 상세정보 조회 api 로 책 상세정보(without page) load
        NaverDetailBookParseResult detailBookByKeyword = findDetailBookByIsbn(isbn);

        // 2. 알라딘으로부터 책 page 정보 load
        Integer pageCount = findPageCountByIsbn(isbn);

        // 3. pageCount 정보를 포함한 Book 반환
        return Book.withoutId(
                detailBookByKeyword.title(),
                isbn,
                detailBookByKeyword.author(),
                false,
                detailBookByKeyword.publisher(),
                detailBookByKeyword.imageUrl(),
                pageCount,
                detailBookByKeyword.description()
        );
    }
}
