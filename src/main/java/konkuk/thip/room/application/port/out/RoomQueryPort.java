package konkuk.thip.room.application.port.out;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import konkuk.thip.room.application.port.out.dto.RoomShowMineQueryDto;
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

    CursorBasedList<RoomShowMineQueryDto> findRecruitingRoomsUserParticipated(Long userId, Cursor cursor);

    CursorBasedList<RoomShowMineQueryDto> findPlayingRoomsUserParticipated(Long userId, Cursor cursor);

    CursorBasedList<RoomShowMineQueryDto> findPlayingAndRecruitingRoomsUserParticipated(Long userId, Cursor cursor);

    CursorBasedList<RoomShowMineQueryDto> findExpiredRoomsUserParticipated(Long userId, Cursor cursor);
}
