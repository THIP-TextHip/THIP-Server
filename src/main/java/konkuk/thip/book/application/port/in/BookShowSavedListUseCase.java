package konkuk.thip.book.application.port.in;

import konkuk.thip.book.application.port.in.dto.BookShowSavedInfoResult;

import java.util.List;

public interface BookShowSavedListUseCase {
    List<BookShowSavedInfoResult> getSavedBookList(Long userId);
}
