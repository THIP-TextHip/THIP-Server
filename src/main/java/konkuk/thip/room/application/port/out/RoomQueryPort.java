package konkuk.thip.room.application.port.out;

import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface RoomQueryPort {
    int countRecruitingRoomsByBookAndStartDateAfter(Long bookId, LocalDate currentDate);
    Page<RoomSearchResponse.RoomSearchResult> searchRoom(String keyword, String category, Pageable pageable);
    Page<RoomGetHomeJoinedListResponse.RoomSearchResult> searchHomeJoinedRooms(Long userId, LocalDate today, Pageable pageable);
}
