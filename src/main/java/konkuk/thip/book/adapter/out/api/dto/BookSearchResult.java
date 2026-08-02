package konkuk.thip.book.adapter.out.api.dto;

import lombok.Builder;

import java.util.List;


public record BookSearchResult(
        List<BookSummary> books,
        int total,
        int start) {

    public static final int PAGE_SIZE = 10;

    @Builder
    public record BookSummary(
            String title,
            String imageUrl,
            String author,
            String publisher,
            String isbn
    ) {}
    public static BookSearchResult of(List<BookSummary> books, int total, int start) {
        return new BookSearchResult(books, total, start);
    }
}
