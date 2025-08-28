package konkuk.thip.book.application.port.in;

import konkuk.thip.book.adapter.in.web.response.BookSelectableListResponse;
import konkuk.thip.book.application.port.in.dto.BookSelectableType;

public interface BookSelectableListUseCase {
    BookSelectableListResponse getSelectableBookList(BookSelectableType bookSelectableType, Long userId, String cursor);
}
