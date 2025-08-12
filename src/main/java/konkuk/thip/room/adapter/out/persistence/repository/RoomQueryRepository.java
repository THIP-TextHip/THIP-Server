package konkuk.thip.room.adapter.out.persistence.repository;

import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import konkuk.thip.room.application.port.out.dto.RoomQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import java.time.LocalDate;

public interface RoomQueryRepository {

    Page<RoomSearchResponse.RoomSearchResult> searchRoom(String keyword, String category, Pageable pageable);

    List<RoomRecruitingDetailViewResponse.RecommendRoom> findOtherRecruitingRoomsByCategoryOrderByStartDateAsc(Long roomId, String category, int count);

    Page<RoomGetHomeJoinedListResponse.RoomSearchResult> searchHomeJoinedRooms(Long userId, LocalDate today, Pageable pageable);

    List<RoomQueryDto> findRecruitingRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findPlayingRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findPlayingAndRecruitingRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findExpiredRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findRoomsByCategoryOrderByStartDateAsc(String category, int limit, Long userId);

    List<RoomQueryDto> findRoomsByCategoryOrderByMemberCount(String category, int limit, Long userId);

    List<RoomQueryDto> findRoomsByIsbnOrderByStartDateAsc(String isbn, LocalDate dateCursor, Long roomIdCursor, int pageSize);
}
