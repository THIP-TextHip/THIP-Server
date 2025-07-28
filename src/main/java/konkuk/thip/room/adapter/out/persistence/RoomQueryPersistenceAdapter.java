package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import konkuk.thip.room.adapter.out.mapper.RoomMapper;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.room.application.port.out.dto.RoomShowMineQueryDto;
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
    private final RoomMapper roomMapper;

    @Override
    public int countRecruitingRoomsByBookAndStartDateAfter(Long bookId, LocalDate currentDate) {
        return roomJpaRepository.countByBookJpaEntity_BookIdAndStartDateAfter(bookId, currentDate);
    }

    @Override
    public Page<RoomSearchResponse.RoomSearchResult> searchRoom(String keyword, String category, Pageable pageable) {
        return roomJpaRepository.searchRoom(keyword, category, pageable);
    }

    @Override
    public List<RoomRecruitingDetailViewResponse.RecommendRoom> findOtherRecruitingRoomsByCategoryOrderByStartDateAsc(Room currentRoom, int count) {
        return roomJpaRepository.findOtherRecruitingRoomsByCategoryOrderByStartDateAsc(currentRoom.getId(), currentRoom.getCategory().getValue(), count);
    }

    @Override
    public Page<RoomGetHomeJoinedListResponse.RoomSearchResult> searchHomeJoinedRooms(Long userId, LocalDate date, Pageable pageable) {
        return roomJpaRepository.searchHomeJoinedRooms(userId, date, pageable);
    }

    @Override
    public CursorBasedList<RoomShowMineQueryDto> findRecruitingRoomsUserParticipated(Long userId, Cursor cursor) {
        LocalDate lastLocalDate = cursor.isFirstRequest() ? null : cursor.getLocalDate(0);
        Long lastId = cursor.isFirstRequest() ? null : cursor.getLong(1);
        int pageSize = cursor.getPageSize();

        List<RoomShowMineQueryDto> roomShowMineQueryDtos = roomJpaRepository.findRecruitingRoomsUserParticipated(userId, lastLocalDate, lastId, pageSize);

        return CursorBasedList.of(roomShowMineQueryDtos, pageSize, roomShowMineQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(
                    roomShowMineQueryDto.endDate().toString(),
                    roomShowMineQueryDto.roomId().toString()
            ));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CursorBasedList<RoomShowMineQueryDto> findPlayingRoomsUserParticipated(Long userId, Cursor cursor) {
        LocalDate lastLocalDate = cursor.isFirstRequest() ? null : cursor.getLocalDate(0);
        Long lastId = cursor.isFirstRequest() ? null : cursor.getLong(1);
        int pageSize = cursor.getPageSize();

        List<RoomShowMineQueryDto> roomShowMineQueryDtos = roomJpaRepository.findPlayingRoomsUserParticipated(userId, lastLocalDate, lastId, pageSize);

        return CursorBasedList.of(roomShowMineQueryDtos, pageSize, roomShowMineQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(
                    roomShowMineQueryDto.endDate().toString(),
                    roomShowMineQueryDto.roomId().toString()
            ));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CursorBasedList<RoomShowMineQueryDto> findPlayingAndRecruitingRoomsUserParticipated(Long userId, Cursor cursor) {
        LocalDate lastLocalDate = cursor.isFirstRequest() ? null : cursor.getLocalDate(0);
        Long lastId = cursor.isFirstRequest() ? null : cursor.getLong(1);
        int pageSize = cursor.getPageSize();

        List<RoomShowMineQueryDto> roomShowMineQueryDtos = roomJpaRepository.findPlayingAndRecruitingRoomsUserParticipated(userId, lastLocalDate, lastId, pageSize);

        return CursorBasedList.of(roomShowMineQueryDtos, pageSize, roomShowMineQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(
                    roomShowMineQueryDto.endDate().toString(),
                    roomShowMineQueryDto.roomId().toString()
            ));
            return nextCursor.toEncodedString();
        });
    }

    @Override
    public CursorBasedList<RoomShowMineQueryDto> findExpiredRoomsUserParticipated(Long userId, Cursor cursor) {
        LocalDate lastLocalDate = cursor.isFirstRequest() ? null : cursor.getLocalDate(0);
        Long lastId = cursor.isFirstRequest() ? null : cursor.getLong(1);
        int pageSize = cursor.getPageSize();

        List<RoomShowMineQueryDto> roomShowMineQueryDtos = roomJpaRepository.findExpiredRoomsUserParticipated(userId, lastLocalDate, lastId, pageSize);

        return CursorBasedList.of(roomShowMineQueryDtos, pageSize, roomShowMineQueryDto -> {
            Cursor nextCursor = new Cursor(List.of(
                    roomShowMineQueryDto.endDate().toString(),
                    roomShowMineQueryDto.roomId().toString()
            ));
            return nextCursor.toEncodedString();
        });
    }

}
