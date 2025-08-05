package konkuk.thip.book.adapter.in.web.response;

import konkuk.thip.book.application.port.in.dto.BookIsSavedResult;
import lombok.Builder;

@Builder
public record BookIsSavedResponse(
        String isbn,
        boolean isSaved
) {
    public static BookIsSavedResponse of(BookIsSavedResult bookIsSavedResult) {
        return new BookIsSavedResponse(bookIsSavedResult.isbn(),bookIsSavedResult.isSaved());
    }
}
