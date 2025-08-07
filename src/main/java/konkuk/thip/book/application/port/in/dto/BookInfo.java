package konkuk.thip.book.application.port.in.dto;

public record BookInfo(
        Long bookId,
        String bookTitle,
        String authorName,
        String publisher,
        String bookImageUrl,
        String isbn
) {
}
