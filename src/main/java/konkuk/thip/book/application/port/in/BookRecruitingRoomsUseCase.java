package konkuk.thip.book.application.port.in;

import konkuk.thip.book.adapter.in.web.response.BookRecruitingRoomsResponse;

public interface BookRecruitingRoomsUseCase {

    BookRecruitingRoomsResponse getRecruitingRoomsWithBook(String isbn, String cursor);
}
