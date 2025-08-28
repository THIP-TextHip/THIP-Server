package konkuk.thip.roompost.adapter.in.web.response;

import konkuk.thip.book.application.port.in.dto.BookPinResult;

public record RecordPinResponse(
        String bookTitle,
        String authorName,
        String bookImageUrl,
        String isbn
) {
    static public RecordPinResponse of(BookPinResult book) {
        return new RecordPinResponse(
                book.bookTitle(),
                book.authorName(),
                book.bookImageUrl(),
                book.isbn()
        );
    }
}