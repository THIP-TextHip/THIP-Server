package konkuk.thip.book.application.port.in.dto;

import konkuk.thip.book.adapter.in.web.response.GetBookMostSearchResponse;
import lombok.Builder;

import java.util.List;

public record BookMostSearchResult(
        List<BookRankInfo> bookList
) {
    @Builder
    public record BookRankInfo(
            int rank,
            String title,
            String imageUrl,
            String isbn
    ) {}

    public static BookMostSearchResult of(List<BookRankInfo> bookList) {
        return new BookMostSearchResult(bookList);
    }
}