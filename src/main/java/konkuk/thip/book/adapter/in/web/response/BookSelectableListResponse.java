package konkuk.thip.book.adapter.in.web.response;

import konkuk.thip.book.application.port.in.dto.BookSelectableResult;

import java.util.List;

public record BookSelectableListResponse(
        List<BookSelectableResult> bookList
) {
    public static BookSelectableListResponse of(List<BookSelectableResult> bookSelectableResults) {
        return new BookSelectableListResponse(bookSelectableResults);
    }
}
