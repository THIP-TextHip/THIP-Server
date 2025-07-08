package konkuk.thip.room.application.port.in;

import konkuk.thip.room.application.port.in.dto.RoomVerifyPasswordQuery;

public interface RoomVerifyPasswordUseCase {
    Void verifyRoomPassword(RoomVerifyPasswordQuery query);
}
