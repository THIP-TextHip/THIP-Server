package konkuk.thip.notification.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import konkuk.thip.notification.application.port.in.dto.FcmTokenRegisterCommand;
import konkuk.thip.common.util.PlatformType;

@Schema(description = "FCM 토큰 등록 요청 DTO")
public record FcmTokenRegisterRequest(
        @NotBlank(message = "디바이스 ID는 필수입니다.")
        @Schema(description = "디바이스 고유 ID", example = "device12345")
        String deviceId,

        @NotBlank(message = "FCM 토큰은 필수입니다.")
        @Schema(description = "FCM 토큰", example = "fcm_token_example_123456")
        String fcmToken,

        @NotBlank(message = "플랫폼 타입은 필수입니다.")
        @Schema(description = "플랫폼 타입 (ANDROID 또는 WEB)", example = "ANDROID")
        String platformType
) {
    public FcmTokenRegisterCommand toCommand(Long userId) {
        return FcmTokenRegisterCommand.builder()
                .deviceId(this.deviceId)
                .fcmToken(this.fcmToken)
                .platformType(PlatformType.from(this.platformType))
                .userId(userId)
                .build();
    }
}
