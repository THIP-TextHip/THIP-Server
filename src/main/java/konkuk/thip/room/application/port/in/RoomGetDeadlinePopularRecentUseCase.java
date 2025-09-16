package konkuk.thip.room.application.port.in;

import konkuk.thip.room.adapter.in.web.response.RoomGetDeadlinePopularRecentResponse;

public interface RoomGetDeadlinePopularRecentUseCase {

    RoomGetDeadlinePopularRecentResponse getDeadlineAndPopularAndRecentRoomList(String category);
}
