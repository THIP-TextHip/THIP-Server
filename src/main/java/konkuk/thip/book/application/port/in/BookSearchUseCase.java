package konkuk.thip.book.application.port.in;

import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;

public interface BookSearchUseCase {

    NaverBookParseResult searchBooks(String keyword, int page);

}
