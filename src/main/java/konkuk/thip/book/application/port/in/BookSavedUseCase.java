package konkuk.thip.book.application.port.in;

import konkuk.thip.book.adapter.in.web.request.PostBookIsSavedRequest;
import konkuk.thip.book.application.port.in.dto.BookIsSavedResult;

public interface BookSavedUseCase {
    BookIsSavedResult isSavedBook(String isbn, PostBookIsSavedRequest postBookIsSavedRequest, Long userId);
}
