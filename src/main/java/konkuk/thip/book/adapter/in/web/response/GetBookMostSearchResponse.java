package konkuk.thip.book.adapter.in.web.response;

import konkuk.thip.book.application.port.in.dto.BookMostSearchResult;
import lombok.Builder;

import java.util.List;

public record GetBookMostSearchResponse(
        List<BookRankInfo> bookList
) {
    @Builder
    public record BookRankInfo(
            int rank,
            String title,
            String imageUrl,
            String isbn
    ) {}

    public static GetBookMostSearchResponse of(BookMostSearchResult bookMostSearchResult) {
        return new GetBookMostSearchResponse(bookMostSearchResult.bookList());
    }
}
