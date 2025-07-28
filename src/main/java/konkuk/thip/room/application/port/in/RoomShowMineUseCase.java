package konkuk.thip.room.application.port.in;

import konkuk.thip.room.adapter.in.web.response.RoomShowMineResponse;

public interface RoomShowMineUseCase {

    RoomShowMineResponse getMyRooms(Long userId, String type, String cursor);
}
