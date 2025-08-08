package konkuk.thip.book.application.port.in.dto;

public record BookSelectableListResult(
        Long bookId,
        String bookTitle,
        String authorName,
        String publisher,
        String bookImageUrl,
        String isbn
) {
}
