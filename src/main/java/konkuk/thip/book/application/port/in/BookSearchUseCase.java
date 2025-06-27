package konkuk.thip.book.application.port.in;

import konkuk.thip.book.adapter.in.web.response.GetBookSearchListResponse;

public interface BookSearchUseCase {

    GetBookSearchListResponse searchBooks(String keyword, int page);

}
