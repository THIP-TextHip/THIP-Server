package konkuk.thip.book.application.port.out;

import konkuk.thip.book.adapter.out.api.dto.BookSearchResult;
import konkuk.thip.book.adapter.out.api.dto.BookDetailResult;
import konkuk.thip.book.domain.Book;

public interface BookApiQueryPort {

    BookSearchResult findBooksByKeyword(String keyword, int start);

    BookDetailResult findDetailBookByIsbn(String isbn);

    Integer findPageCountByIsbn(String isbn);

    Book loadBookWithPageByIsbn(String isbn);
}
