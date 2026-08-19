package konkuk.thip.room.application.port.out;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.application.port.out.dto.RoomParticipantQueryDto;
import konkuk.thip.room.application.port.out.dto.RoomQueryDto;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.domain.value.Category;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomQueryPort {

    int countRecruitingRoomsByBookIsbn(String isbn);

    /**
     * 방 검색
     */
    CursorBasedList<RoomQueryDto> searchRecruitingRoomsByDeadline(String keyword, Cursor cursor, Long viewerId);
    CursorBasedList<RoomQueryDto> searchRecruitingRoomsWithCategoryByDeadline(String keyword, Category category, Cursor cursor, Long viewerId);
    CursorBasedList<RoomQueryDto> searchRecruitingRoomsByMemberCount(String keyword, Cursor cursor, Long viewerId);
    CursorBasedList<RoomQueryDto> searchRecruitingRoomsWithCategoryByMemberCount(String keyword, Category category, Cursor cursor, Long viewerId);

    List<RoomRecruitingDetailViewResponse.RecommendRoom> findOtherRecruitingRoomsByCategoryOrderByStartDateAsc(Room currentRoom, int count, Long viewerId);

    CursorBasedList<RoomParticipantQueryDto> searchHomeJoinedRooms(Long userId, Cursor cursor);

    CursorBasedList<RoomQueryDto> findRecruitingRoomsUserParticipated(Long userId, Cursor cursor);

    CursorBasedList<RoomQueryDto> findPlayingRoomsUserParticipated(Long userId, Cursor cursor);

    CursorBasedList<RoomQueryDto> findPlayingAndRecruitingRoomsUserParticipated(Long userId, Cursor cursor);

    CursorBasedList<RoomQueryDto> findExpiredRoomsUserParticipated(Long userId, Cursor cursor);

    CursorBasedList<RoomQueryDto> findRoomsByIsbnOrderByDeadline(String isbn, Cursor cursor, Long viewerId);

    List<RoomQueryDto> findRoomsByCategoryOrderByDeadline(Category category, int limit, Long viewerId);

    List<RoomQueryDto> findRoomsByCategoryOrderByPopular(Category category, int limit, Long viewerId);

    List<RoomQueryDto> findRoomsByCategoryOrderByRecent(Category category, LocalDateTime createdAfter, int limit, Long viewerId);
    /**
     * 임시 메서드
     * TODO 리펙토링 대상
     */
    String findAliasColorOfCategory(Category category);
}
