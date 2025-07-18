package konkuk.thip.room.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import konkuk.thip.room.application.port.in.dto.RoomJoinCommand;

public record RoomJoinRequest(
        @NotBlank(message = "방 참여 유형 파라미터는 필수입니다..")
        String type
) {
    public RoomJoinCommand toCommand(Long userId, Long roomId) {
        return new RoomJoinCommand(userId, roomId, type);
    }
}
