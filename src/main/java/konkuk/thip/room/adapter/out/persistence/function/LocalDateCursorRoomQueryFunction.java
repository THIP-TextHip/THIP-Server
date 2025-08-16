package konkuk.thip.room.adapter.out.persistence.function;

import konkuk.thip.room.application.port.out.dto.RoomQueryDto;

import java.time.LocalDate;
import java.util.List;

@FunctionalInterface
public interface LocalDateCursorRoomQueryFunction {
    List<RoomQueryDto> apply(LocalDate lastLocalDate, Long lastId, int pageSize);
}
