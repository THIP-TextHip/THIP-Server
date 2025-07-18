package konkuk.thip.room.application.port.out;

import konkuk.thip.room.domain.Category;
import konkuk.thip.room.domain.Room;

public interface RoomCommandPort {

    Room findById(Long id);

    Long save(Room room);

    Category findCategoryByValue(String value);

    void updateMemberCount(Room room);
}
