package konkuk.thip.book.application.port.in.dto;

import konkuk.thip.book.adapter.in.web.response.GetBookMostSearchResponse;

import java.util.List;

public record BookMostSearchResult(
        List<GetBookMostSearchResponse.BookRankInfo> bookList
) {
    public static BookMostSearchResult of(List<GetBookMostSearchResponse.BookRankInfo> bookList) {
        return new BookMostSearchResult(bookList);
    }
}