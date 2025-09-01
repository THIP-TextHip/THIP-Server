package konkuk.thip.book.adapter.in.web.response;

import java.util.List;

public record BookShowSavedListResponse(
        List<BookShowSavedDto> bookList,
        String nextCursor,
        boolean isLast
) {
    public record BookShowSavedDto(
            Long bookId,
            String bookTitle,
            String authorName,
            String publisher,
            String bookImageUrl,
            String isbn,
            boolean isSaved
    ) {
    }
    public static BookShowSavedListResponse of(List<BookShowSavedDto> bookList, String nextCursor, boolean isLast) {
        return new BookShowSavedListResponse(bookList, nextCursor, isLast);
    }
}
