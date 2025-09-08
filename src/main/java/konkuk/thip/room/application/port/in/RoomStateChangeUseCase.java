package konkuk.thip.room.application.port.in;

public interface RoomStateChangeUseCase {
    void changeRoomStateToExpired();
    void changeRoomStateToProgress();
}
