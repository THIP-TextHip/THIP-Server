package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.common.util.EnumMappings;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.adapter.out.persistence.function.IntegerCursorRoomQueryFunction;
import konkuk.thip.room.adapter.out.persistence.function.LocalDateCursorRoomQueryFunction;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.room.application.port.out.dto.RoomQueryDto;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.room.domain.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RoomQueryPersistenceAdapter implements RoomQueryPort {

    private final RoomJpaRepository roomJpaRepository;

    @Override
    public int countRecruitingRoomsByBookAndStartDateAfter(String isbn, LocalDate currentDate) {
        return roomJpaRepository.countActiveRoomsByBookIdAndStartDateAfter(isbn, currentDate);
    }

    @Override
    public CursorBasedList<RoomQueryDto> searchRecruitingRoomsByDeadline(String keyword, Cursor cursor) {
        return findRoomsByDeadlineCursor(cursor, ((lastLocalDate, lastId, pageSize) ->
                roomJpaRepository.findRecruitingRoomsOrderByStartDateAsc(keyword, lastLocalDate, lastId, pageSize)));
    }

    @Override
    public CursorBasedList<RoomQueryDto> searchRecruitingRoomsWithCategoryByDeadline(String keyword, Category category, Cursor cursor) {
        return findRoomsByDeadlineCursor(cursor, (lastLocalDate, lastId, pageSize) ->
                roomJpaRepository.findRecruitingRoomsWithCategoryOrderByStartDateAsc(
                        keyword, category, lastLocalDate, lastId, pageSize
                )
        );
    }

    @Override
    public CursorBasedList<RoomQueryDto> searchRecruitingRoomsByMemberCount(String keyword, Cursor cursor) {
        return findRoomsByMemberCountCursor(cursor, (lastMemberCount, lastId, pageSize) ->
                roomJpaRepository.findRecruitingRoomsOrderByMemberCountDesc(
                        keyword, lastMemberCount, lastId, pageSize
                )
        );
    }

    @Override
    public CursorBasedList<RoomQueryDto> searchRecruitingRoomsWithCategoryByMemberCount(String keyword, Category category, Cursor cursor) {
        return findRoomsByMemberCountCursor(cursor, (lastMemberCount, lastId, pageSize) ->
                roomJpaRepository.findRecruitingRoomsWithCategoryOrderByMemberCountDesc(
                        keyword, category, lastMemberCount, lastId, pageSize
                )
        );
    }

    @Override
    public List<RoomRecruitingDetailViewResponse.RecommendRoom> findOtherRecruitingRoomsByCategoryOrderByStartDateAsc(Room currentRoom, int count) {
        return roomJpaRepository.findOtherRecruitingRoomsByCategoryOrderByStartDateAsc(
                currentRoom.getId(), currentRoom.getCategory(), count);
    }

    @Override
    public Page<RoomGetHomeJoinedListResponse.JoinedRoomInfo> searchHomeJoinedRooms(Long userId, LocalDate date, Pageable pageable) {
        return roomJpaRepository.searchHomeJoinedRooms(userId, date, pageable);
    }

    @Override
    public CursorBasedList<RoomQueryDto> findRecruitingRoomsUserParticipated(Long userId, Cursor cursor) {
        return findRoomsByDeadlineCursor(cursor, (lastLocalDate, lastId, pageSize) ->
                roomJpaRepository.findRecruitingRoomsUserParticipated(userId, lastLocalDate, lastId, pageSize));
    }

    @Override
    public CursorBasedList<RoomQueryDto> findPlayingRoomsUserParticipated(Long userId, Cursor cursor) {
        return findRoomsByDeadlineCursor(cursor, (lastLocalDate, lastId, pageSize) ->
                roomJpaRepository.findPlayingRoomsUserParticipated(userId, lastLocalDate, lastId, pageSize));
    }

    @Override
    public CursorBasedList<RoomQueryDto> findPlayingAndRecruitingRoomsUserParticipated(Long userId, Cursor cursor) {
        return findRoomsByDeadlineCursor(cursor, (lastLocalDate, lastId, pageSize) ->
                roomJpaRepository.findPlayingAndRecruitingRoomsUserParticipated(userId, lastLocalDate, lastId, pageSize));
    }

    @Override
    public CursorBasedList<RoomQueryDto> findExpiredRoomsUserParticipated(Long userId, Cursor cursor) {
        return findRoomsByDeadlineCursor(cursor, (lastLocalDate, lastId, pageSize) ->
                roomJpaRepository.findExpiredRoomsUserParticipated(userId, lastLocalDate, lastId, pageSize));
    }

    @Override
    public CursorBasedList<RoomQueryDto> findRoomsByIsbnOrderByDeadline(String isbn, Cursor cursor) {
        return findRoomsByDeadlineCursor(cursor, (lastLocalDate, lastId, pageSize) ->
                roomJpaRepository.findRoomsByIsbnOrderByStartDateAsc(isbn, lastLocalDate, lastId, pageSize));
    }

    private CursorBasedList<RoomQueryDto> findRoomsByDeadlineCursor(Cursor cursor, LocalDateCursorRoomQueryFunction queryFunction) {
        LocalDate lastLocalDate = cursor.isFirstRequest() ? null : cursor.getLocalDate(0);
        Long lastId = cursor.isFirstRequest() ? null : cursor.getLong(1);
        int pageSize = cursor.getPageSize();

        List<RoomQueryDto> dtos = queryFunction.apply(lastLocalDate, lastId, pageSize);

        return CursorBasedList.of(dtos, pageSize, dto -> {
            Cursor nextCursor = new Cursor(List.of(
                    dto.endDate().toString(),
                    dto.roomId().toString()
            ));
            return nextCursor.toEncodedString();
        });
    }

    private CursorBasedList<RoomQueryDto> findRoomsByMemberCountCursor(Cursor cursor, IntegerCursorRoomQueryFunction queryFunction) {
        Integer lastInteger = cursor.isFirstRequest() ? null : cursor.getInteger(0);
        Long lastId = cursor.isFirstRequest() ? null : cursor.getLong(1);
        int pageSize = cursor.getPageSize();

        List<RoomQueryDto> dtos = queryFunction.apply(lastInteger, lastId, pageSize);

        return CursorBasedList.of(dtos, pageSize, dto -> {
            Cursor nextCursor = new Cursor(List.of(
                    String.valueOf(dto.memberCount()), // 인원수 커서
                    dto.roomId().toString()
            ));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public List<RoomQueryDto> findRoomsByCategoryOrderByDeadline(Category category, int limit, Long userId) {
        return roomJpaRepository.findRoomsByCategoryOrderByStartDateAsc(category, limit, userId);
    }

    @Override
    public List<RoomQueryDto> findRoomsByCategoryOrderByPopular(Category category, int limit, Long userId) {
        return roomJpaRepository.findRoomsByCategoryOrderByMemberCount(category, limit, userId);
    }

    @Override
    public String findAliasColorOfCategory(Category category) {
        return EnumMappings.aliasFrom(category).getColor();
    }
}
