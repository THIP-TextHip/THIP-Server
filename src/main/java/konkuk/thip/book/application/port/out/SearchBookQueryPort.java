package konkuk.thip.book.application.port.out;

import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;

public interface SearchBookQueryPort {
    NaverBookParseResult findBooksByKeyword(String keyword, int start);
}
