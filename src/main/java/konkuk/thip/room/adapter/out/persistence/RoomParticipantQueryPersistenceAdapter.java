package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.room.application.port.out.RoomParticipantQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoomParticipantQueryPersistenceAdapter implements RoomParticipantQueryPort {

    private final RoomParticipantJpaRepository roomParticipantJpaRepository;

    @Override
    public boolean existByUserIdAndRoomId(Long userId, Long roomId) {
        return roomParticipantJpaRepository.existByUserIdAndRoomId(userId, roomId);
    }


}
