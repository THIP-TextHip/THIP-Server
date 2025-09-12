package konkuk.thip.book.application.port.in.dto;

public record BookPinResult(
        String bookTitle,
        String authorName,
        String bookImageUrl,
        String isbn
) {
}
