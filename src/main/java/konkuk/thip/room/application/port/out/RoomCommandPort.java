package konkuk.thip.room.application.port.out;

import konkuk.thip.room.domain.Category;
import konkuk.thip.room.domain.Room;

import java.util.Optional;

public interface RoomCommandPort {

    Room findById(Long id);

    Optional<Room> findByIdOptional(Long id);

    Long save(Room room);

    Category findCategoryByValue(String value);

    void update(Room room);
}
