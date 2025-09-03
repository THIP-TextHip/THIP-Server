package konkuk.thip.notification.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import konkuk.thip.notification.adapter.out.jpa.PlatformType;
import konkuk.thip.notification.application.port.in.dto.FcmTokenRegisterCommand;

@Schema(description = "FCM 토큰 등록 요청 DTO")
public record FcmTokenRequest(
        @Schema(description = "디바이스 고유 ID", example = "device12345")
        String deviceId,

        @Schema(description = "FCM 토큰", example = "fcm_token_example_123456")
        String fcmToken,

        @Schema(description = "플랫폼 타입 (ANDROID 또는 WEB)", example = "ANDROID")
        PlatformType platformType
) {
    public static FcmTokenRegisterCommand toCommand(FcmTokenRequest request, Long userId) {
        return FcmTokenRegisterCommand.builder()
                .deviceId(request.deviceId)
                .fcmToken(request.fcmToken)
                .platformType(request.platformType)
                .userId(userId)
                .build();
    }
}
