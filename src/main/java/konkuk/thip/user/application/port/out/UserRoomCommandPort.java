package konkuk.thip.user.application.port.out;

import konkuk.thip.room.domain.RoomParticipant;

import java.util.List;

public interface UserRoomCommandPort {

    RoomParticipant findByUserIdAndRoomId(Long userId, Long roomId);
    List<RoomParticipant> findAllByRoomId(Long roomId);

}
