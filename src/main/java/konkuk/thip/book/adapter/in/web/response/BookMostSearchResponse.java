package konkuk.thip.book.adapter.in.web.response;

import konkuk.thip.book.application.port.in.dto.BookMostSearchResult;

import java.util.List;

public record BookMostSearchResponse(
        List<BookMostSearchResult.BookRankInfo> bookList
) {
    public static BookMostSearchResponse of(BookMostSearchResult bookMostSearchResult) {
        return new BookMostSearchResponse(bookMostSearchResult.bookList());
    }
}
