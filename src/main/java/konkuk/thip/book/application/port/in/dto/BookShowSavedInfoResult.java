package konkuk.thip.book.application.port.in.dto;

public record BookShowSavedInfoResult(
        Long bookId,
        String bookTitle,
        String authorName,
        String publisher,
        String bookImageUrl,
        String isbn,
        boolean isSaved
) {
}
