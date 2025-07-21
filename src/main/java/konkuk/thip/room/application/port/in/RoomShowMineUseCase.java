package konkuk.thip.room.application.port.in;

import konkuk.thip.room.adapter.in.web.response.RoomShowMineResponse;

import java.time.LocalDate;

public interface RoomShowMineUseCase {

    RoomShowMineResponse getMyRooms(Long userId, String type, LocalDate cursorDate, Long cursorId);
}
