package konkuk.thip.roompost.application.port.in;

import konkuk.thip.roompost.adapter.in.web.response.RoomPostSearchResponse;
import konkuk.thip.roompost.application.port.in.dto.RoomPostSearchQuery;

public interface RoomPostSearchUseCase {

    RoomPostSearchResponse search(RoomPostSearchQuery roomPostSearchQuery);
}
