package konkuk.thip.book.application.service;

import konkuk.thip.book.adapter.in.web.response.BookSelectableListResponse;
import konkuk.thip.book.application.mapper.BookQueryMapper;
import konkuk.thip.book.application.port.in.BookSelectableListUseCase;
import konkuk.thip.book.application.port.in.dto.BookSelectableType;
import konkuk.thip.book.application.port.out.BookQueryPort;
import konkuk.thip.book.application.port.out.dto.BookQueryDto;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookSelectableListService implements BookSelectableListUseCase {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final BookQueryPort bookQueryPort;
    private final BookQueryMapper bookQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public BookSelectableListResponse getSelectableBookList(BookSelectableType bookSelectableType, Long userId, String cursorStr) {

        Cursor cursor = Cursor.from(cursorStr, DEFAULT_PAGE_SIZE);

        CursorBasedList<BookQueryDto> result = switch(bookSelectableType) {
            case SAVED -> bookQueryPort.findSavedBooksBySavedAt(userId, cursor);
            case JOINING -> bookQueryPort.findJoiningRoomsBooksByRoomPercentage(userId, cursor);
        };

        return BookSelectableListResponse.of(bookQueryMapper.toBookSelectableListResponse(result.contents()),
                result.nextCursor(),
                result.isLast());

    }
}
