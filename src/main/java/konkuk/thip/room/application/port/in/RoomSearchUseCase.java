package konkuk.thip.room.application.port.in;

import konkuk.thip.room.adapter.in.web.response.RoomSearchResponse;
import konkuk.thip.room.application.port.in.dto.RoomSearchQuery;

public interface RoomSearchUseCase {

    RoomSearchResponse searchRecruitingRooms(RoomSearchQuery query);
}
