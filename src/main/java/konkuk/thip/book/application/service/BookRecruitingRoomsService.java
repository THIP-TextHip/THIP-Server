package konkuk.thip.book.application.service;

import konkuk.thip.book.adapter.in.web.response.BookRecruitingRoomsResponse;
import konkuk.thip.book.application.mapper.BookQueryMapper;
import konkuk.thip.book.application.port.in.BookRecruitingRoomsUseCase;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.room.application.port.out.dto.RoomQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookRecruitingRoomsService implements BookRecruitingRoomsUseCase {

    private final RoomQueryPort roomQueryPort;
    private final BookQueryMapper bookQueryMapper;

    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    @Transactional(readOnly = true)
    public BookRecruitingRoomsResponse getRecruitingRoomsWithBook(String isbn, String cursorStr) {
        Integer totalRoomCount = (cursorStr == null || cursorStr.isBlank()) ? // 첫 요청 여부 판단
                roomQueryPort.countRecruitingRoomsByBookIsbn(isbn) : null;

        Cursor cursor = Cursor.from(cursorStr, DEFAULT_PAGE_SIZE);
        CursorBasedList<RoomQueryDto> roomDtos = roomQueryPort.findRoomsByIsbnOrderByDeadline(isbn, cursor);

        return BookRecruitingRoomsResponse.of(bookQueryMapper.toRecruitingRoomDtoList(roomDtos.contents()), totalRoomCount,
                roomDtos.nextCursor(), roomDtos.isLast());
    }
}
