package konkuk.thip.room.application.port.out;

import konkuk.thip.room.domain.RoomParticipant;

import java.util.List;

public interface RoomParticipantCommandPort {

    RoomParticipant findByUserIdAndRoomId(Long userId, Long roomId);
    List<RoomParticipant> findAllByRoomId(Long roomId);

}
