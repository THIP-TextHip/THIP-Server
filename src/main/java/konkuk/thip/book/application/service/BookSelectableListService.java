package konkuk.thip.book.application.service;

import konkuk.thip.book.application.mapper.BookQueryMapper;
import konkuk.thip.book.application.port.in.BookSelectableListUseCase;
import konkuk.thip.book.application.port.in.dto.BookInfo;
import konkuk.thip.book.application.port.in.dto.BookSelectableType;
import konkuk.thip.book.application.port.out.BookQueryPort;
import konkuk.thip.book.domain.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookSelectableListService implements BookSelectableListUseCase {

    private final BookQueryPort bookQueryPort;
    private final BookQueryMapper bookQueryMapper;

    @Override
    public List<BookInfo> getSelectableBookList(BookSelectableType bookSelectableType, Long userId) {
        List<Book> bookList = switch(bookSelectableType) {
            case SAVED -> bookQueryPort.findSavedBooksByUserId(userId);
            case JOINING -> bookQueryPort.findJoiningRoomsBooksByUserId(userId);
        };

        return bookQueryMapper.toBookInfoList(bookList);
    }
}
