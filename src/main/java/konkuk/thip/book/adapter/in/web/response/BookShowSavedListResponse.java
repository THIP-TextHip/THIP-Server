package konkuk.thip.book.adapter.in.web.response;

import konkuk.thip.book.application.port.in.dto.BookShowSavedInfoResult;

import java.util.List;

public record BookShowSavedListResponse(
        List<BookShowSavedInfoResult> bookList
) {
    public static BookShowSavedListResponse of(List<BookShowSavedInfoResult> bookSavedInfoResultList) {
        return new BookShowSavedListResponse(bookSavedInfoResultList);
    }
}
