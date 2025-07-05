package konkuk.thip.book.adapter.in.web.response;

import konkuk.thip.book.application.port.in.dto.BookDetailSearchResult;
import konkuk.thip.book.application.port.in.dto.BookIsSavedResult;
import lombok.Builder;

@Builder
public record PostBookIsSavedResponse(
        String isbn,
        boolean isSaved
) {
    public static PostBookIsSavedResponse of(BookIsSavedResult bookIsSavedResult) {
        return new PostBookIsSavedResponse(bookIsSavedResult.isbn(),bookIsSavedResult.isSaved());
    }
}
