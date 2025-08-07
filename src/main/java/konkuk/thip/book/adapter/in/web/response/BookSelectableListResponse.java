package konkuk.thip.book.adapter.in.web.response;

import konkuk.thip.book.application.port.in.dto.BookInfo;

import java.util.List;

public record BookSelectableListResponse(
        List<BookInfo> bookList
) {
    public static BookSelectableListResponse of(List<BookInfo> bookInfos) {
        return new BookSelectableListResponse(bookInfos);
    }
}
