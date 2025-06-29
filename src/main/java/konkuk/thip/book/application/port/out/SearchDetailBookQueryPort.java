package konkuk.thip.book.application.port.out;

import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;

public interface SearchDetailBookQueryPort {
    NaverDetailBookParseResult findDetailBookByKeyword(String isbn);
}
