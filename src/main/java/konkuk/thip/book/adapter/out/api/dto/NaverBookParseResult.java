package konkuk.thip.book.adapter.out.api.dto;

import lombok.Builder;

import java.util.List;


public record NaverBookParseResult(
        List<NaverBook> naverBooks,
        int total,
        int start) {
    @Builder
    public record NaverBook(
            String title,
            String imageUrl,
            String author,
            String publisher,
            String isbn
    ) {}
    public static NaverBookParseResult of(List<NaverBook> books, int total, int start) {
        return new NaverBookParseResult(books, total, start);
    }
}
