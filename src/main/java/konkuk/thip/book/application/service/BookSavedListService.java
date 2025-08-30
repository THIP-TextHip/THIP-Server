package konkuk.thip.book.application.service;

import konkuk.thip.book.adapter.in.web.response.BookShowSavedListResponse;
import konkuk.thip.book.application.mapper.BookQueryMapper;
import konkuk.thip.book.application.port.in.BookShowSavedListUseCase;
import konkuk.thip.book.application.port.out.BookQueryPort;
import konkuk.thip.book.application.port.out.dto.BookQueryDto;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookSavedListService implements BookShowSavedListUseCase {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final BookQueryPort bookQueryPort;
    private final BookQueryMapper bookQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public BookShowSavedListResponse getSavedBookList(Long userId, String cursorStr) {

        Cursor cursor = Cursor.from(cursorStr, DEFAULT_PAGE_SIZE);

        CursorBasedList<BookQueryDto> result = bookQueryPort.findSavedBooksBySavedAt(userId, cursor);

        return BookShowSavedListResponse.of(bookQueryMapper.toBookShowSavedListResponse(result.contents()),
                result.nextCursor(),
                result.isLast());
    }
}
