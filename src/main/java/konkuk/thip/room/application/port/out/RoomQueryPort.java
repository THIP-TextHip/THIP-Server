package konkuk.thip.room.application.port.out;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import konkuk.thip.room.application.port.out.dto.RoomQueryDto;
import konkuk.thip.room.domain.Category;
import konkuk.thip.room.domain.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface RoomQueryPort {

    int countRecruitingRoomsByBookAndStartDateAfter(Long bookId, LocalDate currentDate);

    Page<RoomSearchResponse.RoomSearchResult> searchRoom(String keyword, String category, Pageable pageable);

    List<RoomRecruitingDetailViewResponse.RecommendRoom> findOtherRecruitingRoomsByCategoryOrderByStartDateAsc(Room currentRoom, int count);

    Page<RoomGetHomeJoinedListResponse.RoomSearchResult> searchHomeJoinedRooms(Long userId, LocalDate today, Pageable pageable);

    CursorBasedList<RoomQueryDto> findRecruitingRoomsUserParticipated(Long userId, Cursor cursor);

    CursorBasedList<RoomQueryDto> findPlayingRoomsUserParticipated(Long userId, Cursor cursor);

    CursorBasedList<RoomQueryDto> findPlayingAndRecruitingRoomsUserParticipated(Long userId, Cursor cursor);

    CursorBasedList<RoomQueryDto> findExpiredRoomsUserParticipated(Long userId, Cursor cursor);

    CursorBasedList<RoomQueryDto> findRoomsByIsbnOrderByDeadline(String isbn, Cursor cursor);

    List<RoomQueryDto> findRoomsByCategoryOrderByDeadline(Category category, int limit, Long userId);

    List<RoomQueryDto> findRoomsByCategoryOrderByPopular(Category category, int limit, Long userId);


}
