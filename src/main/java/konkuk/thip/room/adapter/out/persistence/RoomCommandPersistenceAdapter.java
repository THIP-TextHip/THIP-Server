package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomStatus;
import konkuk.thip.room.adapter.out.mapper.RoomMapper;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.domain.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Repository
@RequiredArgsConstructor
public class RoomCommandPersistenceAdapter implements RoomCommandPort {

    private final RoomJpaRepository roomJpaRepository;
    private final BookJpaRepository bookJpaRepository;

    private final RoomMapper roomMapper;

    @Override
    public Room getByIdOrThrow(Long id) {
        RoomJpaEntity roomJpaEntity = roomJpaRepository.findByRoomId(id).orElseThrow(
                () -> new EntityNotFoundException(ROOM_NOT_FOUND)
        );
        return roomMapper.toDomainEntity(roomJpaEntity);
    }

    @Override
    public Optional<Room> findById(Long id) {
        return roomJpaRepository.findByRoomId(id)
                .map(roomMapper::toDomainEntity);
    }

    @Override
    public Long save(Room room) {
        BookJpaEntity bookJpaEntity = bookJpaRepository.findById(room.getBookId()).orElseThrow(
                () -> new EntityNotFoundException(BOOK_NOT_FOUND)
        );

        RoomJpaEntity roomJpaEntity = roomMapper.toJpaEntity(room, bookJpaEntity);
        return roomJpaRepository.save(roomJpaEntity).getRoomId();
    }

    @Override
    public void update(Room room) {
        RoomJpaEntity roomJpaEntity = roomJpaRepository.findByRoomId(room.getId()).orElseThrow(
                () -> new EntityNotFoundException(ROOM_NOT_FOUND)
        );

        roomJpaRepository.save(roomJpaEntity.updateFrom(room));
    }

    @Override
    public int updateRoomStateToExpired(RoomStatus exceptStatus) {
        return roomJpaRepository.updateRoomStatusToExpired(exceptStatus);
    }

    @Override
    public int updateRoomStateToInProgress(RoomStatus fromStatus, RoomStatus toStatus) {
        return roomJpaRepository.updateRoomStatusToInProgress(fromStatus, toStatus);
    }

    @Override
    public List<Room> findProgressTargetRooms(RoomStatus status) {
        List<RoomJpaEntity> roomJpaEntities = roomJpaRepository.findProgressTargetIds(status);
        return roomJpaEntities.stream()
                .map(roomMapper::toDomainEntity)
                .toList();
    }
}
