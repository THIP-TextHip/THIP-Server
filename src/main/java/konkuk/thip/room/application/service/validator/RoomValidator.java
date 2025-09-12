package konkuk.thip.room.application.service.validator;

import konkuk.thip.common.annotation.application.HelperService;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.domain.Room;
import lombok.RequiredArgsConstructor;

@HelperService
@RequiredArgsConstructor
public class RoomValidator {

    private final RoomCommandPort roomCommandPort;

    public void validateRoomExpired(Long roomId) {
        Room room = roomCommandPort.getByIdOrThrow(roomId);
        room.validateRoomExpired();
    }

}
