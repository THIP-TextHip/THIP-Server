package konkuk.thip.book.adapter.in.web.response;

import konkuk.thip.book.application.port.in.dto.BookSelectableListResult;

import java.util.List;

public record BookSelectableListResponse(
        List<BookSelectableListResult> bookList
) {
    public static BookSelectableListResponse of(List<BookSelectableListResult> bookSelectableListResults) {
        return new BookSelectableListResponse(bookSelectableListResults);
    }
}
