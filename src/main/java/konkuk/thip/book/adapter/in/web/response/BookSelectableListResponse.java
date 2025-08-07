package konkuk.thip.book.adapter.in.web.response;

import java.util.List;

public record BookSelectableListResponse(
        List<BookDto> bookList
) {
    public record BookDto(
            Long bookId,
            String bookTitle,
            String authorName,
            String publisher,
            String bookImageUrl,
            String isbn
    ) {
    }
}
