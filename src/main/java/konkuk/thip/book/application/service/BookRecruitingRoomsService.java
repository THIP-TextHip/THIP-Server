package konkuk.thip.book.application.service;

import konkuk.thip.book.adapter.in.web.response.BookRecruitingRoomsResponse;
import konkuk.thip.book.application.port.in.BookRecruitingRoomsUseCase;
import konkuk.thip.book.application.port.out.BookCommandPort;
import konkuk.thip.book.domain.Book;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookRecruitingRoomsService implements BookRecruitingRoomsUseCase {

    private final RoomQueryPort roomQueryPort;
    private final BookCommandPort bookCommandPort;

    @Override
    public BookRecruitingRoomsResponse getRecruitingRoomsWithBook(String isbn, String cursor) {
        Book book = bookCommandPort.getByIsbnOrThrow(isbn);


        var recruitingRoomList = cursorBasedList.getContent().stream()
                .map(room -> BookRecruitingRoomsResponse.RecruitingRoomDto.of(
                        room.getBookImageUrl(),
                        room.getTitle(),
                        room.getMemberCount(),
                        room.getRecruitCount(),
                        room.getRecruitEndDate()))
                .toList();

        return BookRecruitingRoomsResponse.of(recruitingRoomList, cursorBasedList.getNextCursor(), cursorBasedList.isLast());
    }
}
