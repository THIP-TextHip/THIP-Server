package konkuk.thip.room.application.port.in;

import konkuk.thip.room.adapter.in.web.response.RoomGetDeadlinePopularResponse;

public interface RoomGetDeadlinePopularUseCase {

    RoomGetDeadlinePopularResponse getDeadlineAndPopularRoomList(String category, Long userId);
}
