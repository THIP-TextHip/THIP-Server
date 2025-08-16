package konkuk.thip.room.adapter.out.persistence.function;

import konkuk.thip.room.application.port.out.dto.RoomQueryDto;

import java.util.List;

@FunctionalInterface
public interface IntegerCursorRoomQueryFunction {
    List<RoomQueryDto> apply(Integer lastInteger, Long lastId, int pageSize);
}
