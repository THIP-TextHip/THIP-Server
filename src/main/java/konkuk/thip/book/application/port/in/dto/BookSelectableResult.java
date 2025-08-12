package konkuk.thip.book.application.port.in.dto;

public record BookSelectableResult(
        Long bookId,
        String bookTitle,
        String authorName,
        String publisher,
        String bookImageUrl,
        String isbn
) {
}
