package konkuk.thip.room.application.port.out;


public interface RoomParticipantQueryPort {
    boolean existByUserIdAndRoomId(Long userId, Long roomId);
}
