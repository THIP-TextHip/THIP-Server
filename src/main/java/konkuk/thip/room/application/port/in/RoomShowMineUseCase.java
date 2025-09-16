package konkuk.thip.room.application.port.in;

import konkuk.thip.room.adapter.in.web.response.RoomShowMineResponse;
import konkuk.thip.room.application.port.in.dto.MyRoomType;

public interface RoomShowMineUseCase {

    RoomShowMineResponse getMyRooms(Long userId, MyRoomType myRoomType, String cursor);
}
