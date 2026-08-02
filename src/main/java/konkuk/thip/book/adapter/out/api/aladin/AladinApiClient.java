package konkuk.thip.book.adapter.out.api.aladin;

import konkuk.thip.book.adapter.out.api.dto.BookSearchResult;
import konkuk.thip.book.adapter.out.api.dto.BookDetailResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AladinApiClient {

    private final AladinApiUtil aladinApiUtil;

    public BookSearchResult findBooksByKeyword(String keyword, int start) {
        return aladinApiUtil.searchBooks(keyword, start);
    }

    public BookDetailResult findDetailBookByIsbn(String isbn) {
        return aladinApiUtil.getBookDetail(isbn);
    }

    public Integer findPageCountByIsbn(String isbn) {
        return aladinApiUtil.getPageCount(isbn);
    }

    public AladinApiUtil.AladinDetailResult findDetailBookWithPageCountByIsbn(String isbn) {
        return aladinApiUtil.getBookDetailWithPageCount(isbn);
    }
}
