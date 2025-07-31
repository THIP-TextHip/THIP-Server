package konkuk.thip.room.application.port.in;

import konkuk.thip.room.adapter.in.web.response.RoomGetBookPageResponse;

public interface RoomGetBookPageUseCase {
    RoomGetBookPageResponse getBookPage(Long userId, Long roomId);
}
