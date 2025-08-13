package konkuk.thip.record.adapter.in.web.response;

import konkuk.thip.book.application.port.in.dto.BookSelectableResult;

public record RecordPinResponse(
        String bookTitle,
        String authorName,
        String bookImageUrl,
        String isbn
) {
    static public RecordPinResponse of(BookSelectableResult bookSelectableResult) {
        return new RecordPinResponse(
                bookSelectableResult.bookTitle(),
                bookSelectableResult.authorName(),
                bookSelectableResult.bookImageUrl(),
                bookSelectableResult.isbn());
    }
}