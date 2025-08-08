package konkuk.thip.book.application.port.in;

import konkuk.thip.book.application.port.in.dto.BookSelectableListResult;
import konkuk.thip.book.application.port.in.dto.BookSelectableType;

import java.util.List;

public interface BookSelectableListUseCase {
    List<BookSelectableListResult> getSelectableBookList(BookSelectableType bookSelectableType, Long userId);
}
