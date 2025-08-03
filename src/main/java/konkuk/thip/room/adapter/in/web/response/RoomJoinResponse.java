package konkuk.thip.room.adapter.in.web.response;

import konkuk.thip.room.application.port.in.dto.RoomJoinResult;

public record RoomJoinResponse(
        Long roomId,
        String type
) {
    public static RoomJoinResponse of(RoomJoinResult roomJoinResult) {
        return new RoomJoinResponse(
                roomJoinResult.roomId(),
                roomJoinResult.type()
        );
    }
}
