package konkuk.thip.room.application.port.out;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.room.domain.Category;
import konkuk.thip.room.domain.Room;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.ROOM_NOT_FOUND;

public interface RoomCommandPort {

    Optional<Room> findById(Long id);

    default Room getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ROOM_NOT_FOUND));
    }

    Long save(Room room);

    Category findCategoryByValue(String value);

    void update(Room room);
}
