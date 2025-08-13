package konkuk.thip.room.application.port.in;

import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;

public interface RoomSearchUseCase {

    RoomSearchResponse searchRoom(String keyword, String category, String sort, int page, boolean isFinalized, Long userId);
}
