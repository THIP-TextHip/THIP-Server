package konkuk.thip.book.adapter.in.web.response;

import konkuk.thip.book.application.port.in.dto.BookDetailSearchResult;
import lombok.Builder;

@Builder
public record GetBookDetailSearchResponse(
        String title,
        String imageUrl,
        String authorName,
        String publisher,
        String isbn,
        String description,
        int recruitingRoomCount,
        int recruitingReadCount,
        boolean isSaved
) {
    public static GetBookDetailSearchResponse of(BookDetailSearchResult result) {

        return new GetBookDetailSearchResponse(
                result.naverDetailBook().title(),
                result.naverDetailBook().imageUrl(),
                result.naverDetailBook().author(),
                result.naverDetailBook().publisher(),
                result.naverDetailBook().isbn(),
                result.naverDetailBook().description(),
                result.recruitingRoomCount(),
                result.recruitingReadCount(),
                result.isSaved());
    }

}
