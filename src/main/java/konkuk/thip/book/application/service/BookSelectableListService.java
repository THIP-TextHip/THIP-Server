package konkuk.thip.book.application.service;

import konkuk.thip.book.application.port.in.dto.BookSelectableType;
import konkuk.thip.book.adapter.in.web.response.BookSelectableListResponse;
import konkuk.thip.book.application.port.in.BookSelectableListUseCase;
import konkuk.thip.book.application.port.out.BookQueryPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.saved.application.port.out.SavedQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static konkuk.thip.book.application.port.in.dto.BookSelectableType.*;

@Service
@RequiredArgsConstructor
public class BookSelectableListService implements BookSelectableListUseCase {

    private final BookQueryPort bookQueryPort;

    @Override
    public BookSelectableListResponse getSelectableBookList(BookSelectableType bookSelectableType, Long userId) {
        List<Book> bookList = switch(bookSelectableType) {
            case SAVED -> bookQueryPort.findSavedBooksByUserId(userId);
            case JOINING -> bookQueryPort.findJoiningRoomsBooksByUserId(userId);
        };
    }
}
