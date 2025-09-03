package konkuk.thip.notification.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "푸시 알림 설정 변경 요청 DTO")
public record FcmTokenEnableStateChangeRequest(
        @Schema(description = "푸시 알림 수신 여부", example = "true")
        boolean isEnable,

        @Schema(description = "디바이스 고유 ID", example = "device12345")
        String deviceId
) {
}
