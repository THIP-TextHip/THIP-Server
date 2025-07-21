package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.room.adapter.in.web.response.RoomRecruitingDetailViewResponse;
import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import konkuk.thip.room.adapter.in.web.response.RoomShowMineResponse;
import konkuk.thip.room.adapter.out.mapper.RoomMapper;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.application.port.out.RoomQueryPort;
import konkuk.thip.room.application.port.out.dto.CursorSliceOfMyRoomView;
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
    public CursorSliceOfMyRoomView<RoomShowMineResponse.MyRoom> findRecruitingRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize) {
        return roomJpaRepository.findRecruitingRoomsUserParticipated(userId, dateCursor, roomIdCursor, pageSize);
    }

    @Override
    public CursorSliceOfMyRoomView<RoomShowMineResponse.MyRoom> findPlayingRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize) {
        return roomJpaRepository.findPlayingRoomsUserParticipated(userId, dateCursor, roomIdCursor, pageSize);
    }

    @Override
    public CursorSliceOfMyRoomView<RoomShowMineResponse.MyRoom> findPlayingAndRecruitingRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize) {
        return roomJpaRepository.findPlayingAndRecruitingRoomsUserParticipated(userId, dateCursor, roomIdCursor, pageSize);
    }

    @Override
    public CursorSliceOfMyRoomView<RoomShowMineResponse.MyRoom> findExpiredRoomsUserParticipated(Long userId, LocalDate dateCursor, Long roomIdCursor, int pageSize) {
        return roomJpaRepository.findExpiredRoomsUserParticipated(userId, dateCursor, roomIdCursor, pageSize);
    }

}
