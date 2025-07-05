package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.mapper.RoomMapper;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.domain.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static konkuk.thip.common.exception.code.ErrorCode.ROOM_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class RoomCommandPersistenceAdapter implements RoomCommandPort {

    private final RoomJpaRepository roomJpaRepository;
    private final RoomMapper roomMapper;

    @Override
    public Room findById(Long id) {
        RoomJpaEntity roomJpaEntity = roomJpaRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(ROOM_NOT_FOUND)
        );

        return roomMapper.toDomainEntity(roomJpaEntity);
    }
}
