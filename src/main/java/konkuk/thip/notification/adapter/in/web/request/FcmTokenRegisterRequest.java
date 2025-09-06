package konkuk.thip.notification.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import konkuk.thip.notification.domain.value.PlatformType;
import konkuk.thip.notification.application.port.in.dto.FcmTokenRegisterCommand;

@Schema(description = "FCM 토큰 등록 요청 DTO")
public record FcmTokenRegisterRequest(
        @NotBlank
        @Schema(description = "디바이스 고유 ID", example = "device12345")
        String deviceId,

        @NotBlank
        @Schema(description = "FCM 토큰", example = "fcm_token_example_123456")
        String fcmToken,

        @NotNull
        @Schema(description = "플랫폼 타입 (ANDROID 또는 WEB)", example = "ANDROID")
        PlatformType platformType
) {
    public FcmTokenRegisterCommand toCommand(Long userId) {
        return FcmTokenRegisterCommand.builder()
                .deviceId(this.deviceId)
                .fcmToken(this.fcmToken)
                .platformType(this.platformType)
                .userId(userId)
                .build();
    }
}
