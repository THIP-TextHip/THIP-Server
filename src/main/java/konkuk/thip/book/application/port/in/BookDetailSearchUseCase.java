package konkuk.thip.book.application.port.in;

import konkuk.thip.book.application.port.in.dto.BookDetailSearchResult;

public interface BookDetailSearchUseCase {

    BookDetailSearchResult searchDetailBooks(String isbn);

}
