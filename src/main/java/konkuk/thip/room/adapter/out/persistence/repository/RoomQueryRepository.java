package konkuk.thip.room.adapter.out.persistence.repository;

import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.application.port.out.dto.RoomParticipantQueryDto;
import konkuk.thip.room.application.port.out.dto.RoomQueryDto;
import konkuk.thip.room.domain.value.Category;

import java.time.LocalDateTime;
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

    List<RoomParticipantQueryDto> findHomeJoinedRoomsByUserPercentage(Long userId, Double userPercentageCursor, LocalDate startDateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findRecruitingRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findPlayingRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findPlayingAndRecruitingRoomsUserParticipated(Long userId, Integer priorityCursor, LocalDate dateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findExpiredRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findRoomsByCategoryOrderByStartDateAsc(Category category, int limit);

    List<RoomQueryDto> findRoomsByCategoryOrderByMemberCount(Category category, int limit);

    List<RoomQueryDto> findRoomsByCategoryOrderByCreatedAtDesc(Category category, LocalDateTime createdAfter, int limit);

    List<RoomQueryDto> findRoomsByIsbnOrderByStartDateAsc(String isbn, LocalDate dateCursor, Long roomIdCursor, int pageSize);
}
