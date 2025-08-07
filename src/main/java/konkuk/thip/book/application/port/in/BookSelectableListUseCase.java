package konkuk.thip.book.application.port.in;

import konkuk.thip.book.application.port.in.dto.BookInfo;
import konkuk.thip.book.application.port.in.dto.BookSelectableType;

import java.util.List;

public interface BookSelectableListUseCase {
    List<BookInfo> getSelectableBookList(BookSelectableType bookSelectableType, Long userId);
}
