package konkuk.thip.book.application.port.in;

import konkuk.thip.book.adapter.in.web.response.BookShowSavedListResponse;

public interface BookShowSavedListUseCase {
    BookShowSavedListResponse getSavedBookList(Long userId, String cursor);
}
