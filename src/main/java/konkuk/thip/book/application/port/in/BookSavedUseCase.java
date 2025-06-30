package konkuk.thip.book.application.port.in;

import jakarta.validation.constraints.Pattern;
import konkuk.thip.book.adapter.out.api.dto.NaverBookParseResult;
import konkuk.thip.book.application.port.in.dto.BookDetailSearchResult;
import konkuk.thip.book.application.port.in.dto.BookIsSavedResult;

public interface BookSavedUseCase {
    BookIsSavedResult isSavedBook(String isbn, boolean type, Long userId);
}
