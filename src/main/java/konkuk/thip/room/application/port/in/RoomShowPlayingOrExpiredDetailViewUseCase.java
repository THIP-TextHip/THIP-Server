package konkuk.thip.room.application.port.in;

import konkuk.thip.room.adapter.in.web.response.RoomPlayingOrExpiredDetailViewResponse;

public interface RoomShowPlayingOrExpiredDetailViewUseCase {

    RoomPlayingOrExpiredDetailViewResponse getPlayingOrExpiredRoomDetailView(Long userId, Long roomId);
}
