package konkuk.thip.room.adapter.out.persistence.repository;

import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.application.port.out.dto.RoomQueryDto;
import konkuk.thip.room.domain.value.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import java.time.LocalDate;

public interface RoomQueryRepository {

    /**
     * 방 검색
     */
    List<RoomQueryDto> findRecruitingRoomsOrderByStartDateAsc(String keyword, LocalDate lastStartDate, Long roomId, int pageSize);
    List<RoomQueryDto> findRecruitingRoomsWithCategoryOrderByStartDateAsc(String keyword, Category category, LocalDate lastStartDate, Long roomId, int pageSize);
    List<RoomQueryDto> findRecruitingRoomsOrderByMemberCountDesc(String keyword, Integer lastMemberCount, Long roomId, int pageSize);
    List<RoomQueryDto> findRecruitingRoomsWithCategoryOrderByMemberCountDesc(String keyword, Category category, Integer lastMemberCount, Long roomId, int pageSize);

    List<RoomRecruitingDetailViewResponse.RecommendRoom> findOtherRecruitingRoomsByCategoryOrderByStartDateAsc(Long roomId, Category category, int count);

    Page<RoomGetHomeJoinedListResponse.JoinedRoomInfo> searchHomeJoinedRooms(Long userId, LocalDate today, Pageable pageable);

    List<RoomQueryDto> findRecruitingRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findPlayingRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findPlayingAndRecruitingRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findExpiredRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findRoomsByCategoryOrderByStartDateAsc(Category category, int limit, Long userId);

    List<RoomQueryDto> findRoomsByCategoryOrderByMemberCount(Category category, int limit, Long userId);

    List<RoomQueryDto> findRoomsByIsbnOrderByStartDateAsc(String isbn, LocalDate dateCursor, Long roomIdCursor, int pageSize);
}
