package konkuk.thip.room.application.port.in;

import konkuk.thip.room.application.port.in.dto.RoomJoinCommand;

public interface RoomJoinUsecase {

    void changeJoinState(RoomJoinCommand roomJoinCommand);

}
