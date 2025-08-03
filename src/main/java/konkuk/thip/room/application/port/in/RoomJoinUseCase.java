package konkuk.thip.room.application.port.in;

import konkuk.thip.room.application.port.in.dto.RoomJoinCommand;
import konkuk.thip.room.application.port.in.dto.RoomJoinResult;

public interface RoomJoinUseCase {

    RoomJoinResult changeJoinState(RoomJoinCommand roomJoinCommand);

}
