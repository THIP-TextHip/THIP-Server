package konkuk.thip.room.application.port.out;

import konkuk.thip.room.domain.RoomParticipant;

import java.util.List;
import java.util.Optional;

public interface RoomParticipantCommandPort {

    RoomParticipant findByUserIdAndRoomId(Long userId, Long roomId);
    List<RoomParticipant> findAllByRoomId(Long roomId);

    void save(RoomParticipant roomParticipant);

    void deleteByUserIdAndRoomId(Long userId, Long roomId);

    Optional<RoomParticipant> findByUserIdAndRoomIdOptional(Long userId, Long roomId);

}
