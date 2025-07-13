package konkuk.thip.room.application.port.in;

import konkuk.thip.room.adapter.in.web.response.RoomGetHomeJoinedListResponse;
import konkuk.thip.room.application.port.in.dto.RoomGetHomeJoinedListQuery;

public interface RoomGetHomeJoinedListUseCase {
    RoomGetHomeJoinedListResponse getHomeJoinedRoomList(RoomGetHomeJoinedListQuery roomGetHomeJoinedListQuery);
}