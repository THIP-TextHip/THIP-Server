package konkuk.thip.room.application.port.in;

import konkuk.thip.room.adapter.in.web.response.RoomPlayingDetailViewResponse;

public interface RoomShowPlayingDetailViewUseCase {

    RoomPlayingDetailViewResponse getPlayingRoomDetailView(Long userId, Long roomId);
}
