package konkuk.thip.room.application.port.in;

import konkuk.thip.room.adapter.in.web.response.RoomVerifyPasswordResponse;
import konkuk.thip.room.application.port.in.dto.RoomVerifyPasswordQuery;

public interface RoomVerifyPasswordUseCase {
    RoomVerifyPasswordResponse verifyRoomPassword(RoomVerifyPasswordQuery query);
}
