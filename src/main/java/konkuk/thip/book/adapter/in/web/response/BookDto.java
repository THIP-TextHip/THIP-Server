package konkuk.thip.book.adapter.in.web.response;

public record BookDto(
    String title,
    String imageUrl,
    String authorName,
    String publisher,
    String isbn
) {}
