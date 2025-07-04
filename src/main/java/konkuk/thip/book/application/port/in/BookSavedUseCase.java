package konkuk.thip.book.application.port.in;

import konkuk.thip.book.application.port.in.dto.BookIsSavedResult;

public interface BookSavedUseCase {
    BookIsSavedResult changeSavedBook(String isbn, boolean isSave, Long userId);
}
