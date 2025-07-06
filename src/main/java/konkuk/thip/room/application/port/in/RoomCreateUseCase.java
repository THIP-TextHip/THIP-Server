package konkuk.thip.room.application.port.in;

import konkuk.thip.room.application.port.in.dto.RoomCreateCommand;

public interface RoomCreateUseCase {

    Long createRoom(RoomCreateCommand command);
}
