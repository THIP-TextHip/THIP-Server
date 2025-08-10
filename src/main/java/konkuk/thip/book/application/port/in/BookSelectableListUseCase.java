package konkuk.thip.book.application.port.in;

import konkuk.thip.book.application.port.in.dto.BookSelectableResult;
import konkuk.thip.book.application.port.in.dto.BookSelectableType;

import java.util.List;

public interface BookSelectableListUseCase {
    List<BookSelectableResult> getSelectableBookList(BookSelectableType bookSelectableType, Long userId);
}
