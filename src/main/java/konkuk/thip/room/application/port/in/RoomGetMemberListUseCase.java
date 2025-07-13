package konkuk.thip.room.application.port.in;

import konkuk.thip.room.adapter.in.web.response.RoomGetMemberListResponse;

public interface RoomGetMemberListUseCase {
    RoomGetMemberListResponse getRoomMemberList(Long roomId);
}
