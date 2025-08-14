package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import konkuk.thip.room.adapter.out.persistence.function.RoomQueryFunction;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.room.application.port.out.dto.RoomQueryDto;
import konkuk.thip.room.domain.Category;
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
    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public int countRecruitingRoomsByBookAndStartDateAfter(String isbn, LocalDate currentDate) {
        return roomJpaRepository.countActiveRoomsByBookIdAndStartDateAfter(isbn, currentDate);
    }

    @Override
    public Page<RoomSearchResponse.RoomSearchResult> searchRoom(String keyword, String category, Pageable pageable) {
        return roomJpaRepository.searchRoom(keyword, category, pageable);
    }

    @Override
    public List<RoomRecruitingDetailViewResponse.RecommendRoom> findOtherRecruitingRoomsByCategoryOrderByStartDateAsc(Room currentRoom, int count) {
        return roomJpaRepository.findOtherRecruitingRoomsByCategoryOrderByStartDateAsc(
                currentRoom.getId(), currentRoom.getCategory().getValue(), count);
    }

    @Override
    public Page<RoomGetHomeJoinedListResponse.RoomSearchResult> searchHomeJoinedRooms(Long userId, LocalDate date, Pageable pageable) {
        return roomJpaRepository.searchHomeJoinedRooms(userId, date, pageable);
    }

    @Override
    public CursorBasedList<RoomQueryDto> findRecruitingRoomsUserParticipated(Long userId, Cursor cursor) {
        return findRooms(cursor, (lastLocalDate, lastId, pageSize) ->
                roomJpaRepository.findRecruitingRoomsUserParticipated(userId, lastLocalDate, lastId, pageSize));
    }

    @Override
    public CursorBasedList<RoomQueryDto> findPlayingRoomsUserParticipated(Long userId, Cursor cursor) {
        return findRooms(cursor, (lastLocalDate, lastId, pageSize) ->
                roomJpaRepository.findPlayingRoomsUserParticipated(userId, lastLocalDate, lastId, pageSize));
    }

    @Override
    public CursorBasedList<RoomQueryDto> findPlayingAndRecruitingRoomsUserParticipated(Long userId, Cursor cursor) {
        return findRooms(cursor, (lastLocalDate, lastId, pageSize) ->
                roomJpaRepository.findPlayingAndRecruitingRoomsUserParticipated(userId, lastLocalDate, lastId, pageSize));
    }

    @Override
    public CursorBasedList<RoomQueryDto> findExpiredRoomsUserParticipated(Long userId, Cursor cursor) {
        return findRooms(cursor, (lastLocalDate, lastId, pageSize) ->
                roomJpaRepository.findExpiredRoomsUserParticipated(userId, lastLocalDate, lastId, pageSize));
    }

    @Override
    public CursorBasedList<RoomQueryDto> findRoomsByIsbnOrderByDeadline(String isbn, Cursor cursor) {
        return findRooms(cursor, (lastLocalDate, lastId, pageSize) ->
                roomJpaRepository.findRoomsByIsbnOrderByStartDateAsc(isbn, lastLocalDate, lastId, pageSize));
    }

    private CursorBasedList<RoomQueryDto> findRooms(Cursor cursor, RoomQueryFunction queryFunction) {
        LocalDate lastLocalDate = cursor.isFirstRequest() ? null : cursor.getLocalDate(0);
        Long lastId = cursor.isFirstRequest() ? null : cursor.getLong(1);
        int pageSize = cursor.getPageSize();

        List<RoomQueryDto> dtos = queryFunction.apply(lastLocalDate, lastId, pageSize);

        return CursorBasedList.of(dtos, pageSize, roomShowMineQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(
                    roomShowMineQueryDto.endDate().toString(),
                    roomShowMineQueryDto.roomId().toString()
            ));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public List<RoomQueryDto> findRoomsByCategoryOrderByDeadline(Category category, int limit, Long userId) {
        return roomJpaRepository.findRoomsByCategoryOrderByStartDateAsc(category.getValue(), limit, userId);
    }

    @Override
    public List<RoomQueryDto> findRoomsByCategoryOrderByPopular(Category category, int limit, Long userId) {
        return roomJpaRepository.findRoomsByCategoryOrderByMemberCount(category.getValue(), limit, userId);
    }

    // TODO : 리펙토링 대상
    @Override
    public String findAliasColorOfCategory(Category category) {
        return categoryJpaRepository.findAliasColorByValue(category.getValue()).orElseThrow(
                () -> new EntityNotFoundException(ErrorCode.CATEGORY_NOT_FOUND)
        );
    }
}
