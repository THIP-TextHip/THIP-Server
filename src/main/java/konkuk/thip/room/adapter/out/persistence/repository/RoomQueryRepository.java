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
    List<RoomQueryDto> findRecruitingRoomsOrderByStartDateAsc(String keyword, LocalDate lastStartDate, Long roomId, int pageSize, Long viewerId);
    List<RoomQueryDto> findRecruitingRoomsWithCategoryOrderByStartDateAsc(String keyword, Category category, LocalDate lastStartDate, Long roomId, int pageSize, Long viewerId);
    List<RoomQueryDto> findRecruitingRoomsOrderByMemberCountDesc(String keyword, Integer lastMemberCount, Long roomId, int pageSize, Long viewerId);
    List<RoomQueryDto> findRecruitingRoomsWithCategoryOrderByMemberCountDesc(String keyword, Category category, Integer lastMemberCount, Long roomId, int pageSize, Long viewerId);

    List<RoomRecruitingDetailViewResponse.RecommendRoom> findOtherRecruitingRoomsByCategoryOrderByStartDateAsc(Long roomId, Category category, int count, Long viewerId);

    List<RoomParticipantQueryDto> findHomeJoinedRoomsByUserPercentage(Long userId, Double userPercentageCursor, LocalDate startDateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findRecruitingRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findPlayingRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findPlayingAndRecruitingRoomsUserParticipated(Long userId, Integer priorityCursor, LocalDate dateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findExpiredRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize);

    List<RoomQueryDto> findRoomsByCategoryOrderByStartDateAsc(Category category, int limit, Long viewerId);

    List<RoomQueryDto> findRoomsByCategoryOrderByMemberCount(Category category, int limit, Long viewerId);

    List<RoomQueryDto> findRoomsByCategoryOrderByCreatedAtDesc(Category category, LocalDateTime createdAfter, int limit, Long viewerId);

    List<RoomQueryDto> findRoomsByIsbnOrderByStartDateAsc(String isbn, LocalDate dateCursor, Long roomIdCursor, int pageSize, Long viewerId);
}
