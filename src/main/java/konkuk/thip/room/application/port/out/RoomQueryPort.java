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
    CursorBasedList<RoomQueryDto> searchRecruitingRoomsByDeadline(String keyword, Cursor cursor);
    CursorBasedList<RoomQueryDto> searchRecruitingRoomsWithCategoryByDeadline(String keyword, Category category, Cursor cursor);
    CursorBasedList<RoomQueryDto> searchRecruitingRoomsByMemberCount(String keyword, Cursor cursor);
    CursorBasedList<RoomQueryDto> searchRecruitingRoomsWithCategoryByMemberCount(String keyword, Category category, Cursor cursor);

    List<RoomRecruitingDetailViewResponse.RecommendRoom> findOtherRecruitingRoomsByCategoryOrderByStartDateAsc(Room currentRoom, int count);

    CursorBasedList<RoomParticipantQueryDto> searchHomeJoinedRooms(Long userId, Cursor cursor);

    CursorBasedList<RoomQueryDto> findRecruitingRoomsUserParticipated(Long userId, Cursor cursor);

    CursorBasedList<RoomQueryDto> findPlayingRoomsUserParticipated(Long userId, Cursor cursor);

    CursorBasedList<RoomQueryDto> findPlayingAndRecruitingRoomsUserParticipated(Long userId, Cursor cursor);

    CursorBasedList<RoomQueryDto> findExpiredRoomsUserParticipated(Long userId, Cursor cursor);

    CursorBasedList<RoomQueryDto> findRoomsByIsbnOrderByDeadline(String isbn, Cursor cursor);

    List<RoomQueryDto> findRoomsByCategoryOrderByDeadline(Category category, int limit);

    List<RoomQueryDto> findRoomsByCategoryOrderByPopular(Category category, int limit);

    List<RoomQueryDto> findRoomsByCategoryOrderByRecent(Category category, LocalDateTime now, int limit);
    /**
     * 임시 메서드
     * TODO 리펙토링 대상
     */
    String findAliasColorOfCategory(Category category);
}
