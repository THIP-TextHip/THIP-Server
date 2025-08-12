package konkuk.thip.book.application.port.in;

import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import konkuk.thip.book.application.port.in.dto.BookDetailSearchResult;

public interface BookSearchUseCase {

    NaverBookParseResult searchBooks(String keyword, int page, Long userId, boolean isFinalized);
    BookDetailSearchResult searchDetailBooks(String isbn, Long userId);
}
