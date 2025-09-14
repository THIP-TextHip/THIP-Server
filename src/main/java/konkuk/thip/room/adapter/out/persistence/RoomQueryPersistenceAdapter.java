package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.common.util.EnumMappings;
import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.adapter.out.persistence.function.IntegerCursorRoomQueryFunction;
import konkuk.thip.room.adapter.out.persistence.function.LocalDateCursorRoomQueryFunction;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.room.application.port.out.dto.RoomParticipantQueryDto;
import konkuk.thip.room.application.port.out.dto.RoomQueryDto;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.room.domain.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RoomQueryPersistenceAdapter implements RoomQueryPort {

    private final RoomJpaRepository roomJpaRepository;

    @Override
    public int countRecruitingRoomsByBookIsbn(String isbn) {
        return roomJpaRepository.countRecruitingRoomsByBookIsbn(isbn);
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
    public CursorBasedList<RoomParticipantQueryDto> searchHomeJoinedRooms(Long userId, Cursor cursor) {
        Double lastUserPercentage = cursor.isFirstRequest() ? null : cursor.getDouble(0);
        LocalDate lastStartDate = cursor.isFirstRequest() ? null : cursor.getLocalDate(1);
        Long lastRoomId = cursor.isFirstRequest() ? null : cursor.getLong(2);
        int pageSize = cursor.getPageSize();

        List<RoomParticipantQueryDto> dtos = roomJpaRepository.findHomeJoinedRoomsByUserPercentage(
                userId, lastUserPercentage, lastStartDate, lastRoomId, pageSize
        );

        return CursorBasedList.of(dtos, pageSize, dto -> {
            Cursor nextCursor = new Cursor(List.of(
                    dto.userPercentage().toString(),         // 내림차순 필드, 정렬순서 1
                    dto.startDate().toString(),              // 오름차순 필드, 정렬순서 2
                    dto.roomId().toString()                   // 고유 ID, 중복 방지용
            ));
            return nextCursor.toEncodedString();
        });
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
        Integer lastPriority = cursor.isFirstRequest() ? null : cursor.getInteger(0);
        LocalDate lastLocalDate = cursor.isFirstRequest() ? null : cursor.getLocalDate(1);
        Long lastId = cursor.isFirstRequest() ? null : cursor.getLong(2);
        int pageSize = cursor.getPageSize();

        List<RoomQueryDto> dtos = roomJpaRepository.findPlayingAndRecruitingRoomsUserParticipated(
                userId, lastPriority, lastLocalDate, lastId, pageSize
        );

        return CursorBasedList.of(dtos, pageSize, dto -> {
            int priority = dto.startDate().isAfter(LocalDate.now()) ? 1 : 0;   // 0 : 진행중인 방, 1 : 모집중인 방    // TODO : dto에 RoomStatus 도입되면 수정해야함

            Cursor nextCursor = new Cursor(List.of(
                    String.valueOf(priority),
                    dto.endDate().toString(),
                    dto.roomId().toString()
            ));
            return nextCursor.toEncodedString();
        });
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
    public List<RoomQueryDto> findRoomsByCategoryOrderByDeadline(Category category, int limit) {
        return roomJpaRepository.findRoomsByCategoryOrderByStartDateAsc(category, limit);
    }

    @Override
    public List<RoomQueryDto> findRoomsByCategoryOrderByPopular(Category category, int limit) {
        return roomJpaRepository.findRoomsByCategoryOrderByMemberCount(category, limit);
    }

    @Override
    public List<RoomQueryDto> findRoomsByCategoryOrderByRecent(Category category, int limit) {
        return roomJpaRepository.findRoomsByCategoryOrderByCreatedAtDesc(category, limit);
    }

    @Override
    public String findAliasColorOfCategory(Category category) {
        return EnumMappings.aliasFrom(category).getColor();
    }
}
