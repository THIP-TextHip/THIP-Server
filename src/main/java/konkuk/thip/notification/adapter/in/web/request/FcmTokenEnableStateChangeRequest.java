package konkuk.thip.notification.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import konkuk.thip.notification.application.port.in.dto.FcmEnableStateChangeCommand;

@Schema(description = "푸시 알림 설정 변경 요청 DTO")
public record FcmTokenEnableStateChangeRequest(
        @NotNull
        @Schema(description = "푸시 알림 수신 여부", example = "true")
        boolean enable,

        @NotBlank
        @Schema(description = "디바이스 고유 ID", example = "device12345")
        String deviceId
) {
        public FcmEnableStateChangeCommand toCommand(Long userId) {
                return new FcmEnableStateChangeCommand(
                        userId,
                        this.enable,
                        this.deviceId
                );
        }
}
