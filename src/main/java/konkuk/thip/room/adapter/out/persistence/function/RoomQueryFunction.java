package konkuk.thip.room.adapter.out.persistence.function;

import konkuk.thip.room.application.port.out.dto.RoomQueryDto;

import java.time.LocalDate;
import java.util.List;

@FunctionalInterface
public interface RoomQueryFunction {
    List<RoomQueryDto> apply(Long userId, LocalDate lastLocalDate, Long lastId, int pageSize);
}
