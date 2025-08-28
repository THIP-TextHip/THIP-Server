package konkuk.thip.book.adapter.in.web.response;

import java.util.List;

public record BookSelectableListResponse(
        List<BookSelectableDto> bookList,
        String nextCursor,
        boolean isLast
) {
    public record BookSelectableDto(
            Long bookId,
            String bookTitle,
            String authorName,
            String publisher,
            String bookImageUrl,
            String isbn
    ) {}

    public static BookSelectableListResponse of(List<BookSelectableDto> bookList, String nextCursor, boolean isLast) {
        return new BookSelectableListResponse(bookList, nextCursor, isLast);
    }
}
