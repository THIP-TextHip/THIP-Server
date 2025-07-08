package konkuk.thip.book.application.port.in;

import konkuk.thip.book.application.port.in.dto.BookMostSearchResult;

public interface BookMostSearchUseCase {
    BookMostSearchResult getMostSearchedBooks(Long userId);
}
