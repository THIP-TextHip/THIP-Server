package konkuk.thip.notification.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import konkuk.thip.notification.application.port.in.dto.FcmTokenDeleteCommand;

@Schema(description = "푸시 알림 설정 삭제 요청 DTO")
public record FcmTokenDeleteRequest(
        @NotBlank
        @Schema(description = "디바이스 고유 ID", example = "device12345")
        String deviceId
) {
    public FcmTokenDeleteCommand toCommand(Long userId) {
        return new FcmTokenDeleteCommand(
                userId,
                this.deviceId
        );
    }
}
