package konkuk.thip.book.adapter.out.api.dto;

import konkuk.thip.book.domain.Book;

import java.util.List;


public record NaverBookParseResult(
        List<Book> books,
        int total,
        int start) {
    public static NaverBookParseResult of(List<Book> books, int total, int start) {
        return new NaverBookParseResult(books, total, start);
    }
}
