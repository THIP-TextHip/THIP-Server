package konkuk.thip.room.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import konkuk.thip.room.application.port.in.dto.RoomJoinCommand;
import konkuk.thip.room.application.port.in.dto.RoomJoinType;

@Schema(description = "방 참여/취소 요청 DTO")
public record RoomJoinRequest(
        @Schema(description = "방 참여 유형 (join: 참여, cancel: 취소)", example = "join")
        @NotBlank(message = "방 참여 유형 파라미터는 필수입니다.")
        String type
) {
    public RoomJoinCommand toCommand(Long userId, Long roomId) {
        return new RoomJoinCommand(userId, roomId, RoomJoinType.from(type));
    }
}
