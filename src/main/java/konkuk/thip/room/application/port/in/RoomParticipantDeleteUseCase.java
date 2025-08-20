package konkuk.thip.room.application.port.in;

public interface RoomParticipantDeleteUseCase {
    Void leaveRoom(Long userId, Long roomId);
}
