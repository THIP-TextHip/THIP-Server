package konkuk.thip.book.adapter.in.web.response;

import konkuk.thip.book.application.port.in.dto.BookDetailSearchResult;
import lombok.Builder;

@Builder
public record BookDetailSearchResponse(
        String title,
        String imageUrl,
        String authorName,
        String publisher,
        String isbn,
        String description,
        int recruitingRoomCount,
        int readCount,
        boolean isSaved
) {
    public static BookDetailSearchResponse of(BookDetailSearchResult result) {

        return new BookDetailSearchResponse(
                result.bookDetail().title(),
                result.bookDetail().imageUrl(),
                result.bookDetail().author(),
                result.bookDetail().publisher(),
                result.bookDetail().isbn(),
                result.bookDetail().description(),
                result.recruitingRoomCount(),
                result.readCount(),
                result.isSaved());
    }

}
