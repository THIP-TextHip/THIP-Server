package konkuk.thip.book.application.port.out;

import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import konkuk.thip.book.adapter.out.api.dto.NaverDetailBookParseResult;
import konkuk.thip.book.domain.Book;

public interface BookApiQueryPort {
    NaverBookParseResult findBooksByKeyword(String keyword, int start);
    NaverDetailBookParseResult findDetailBookByKeyword(String isbn);

    Integer findPageCountByIsbn(String isbn);

    Book loadBookWithPageByIsbn(String isbn);
}
