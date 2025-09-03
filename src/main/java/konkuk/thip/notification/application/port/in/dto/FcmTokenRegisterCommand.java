package konkuk.thip.notification.application.port.in.dto;

import konkuk.thip.notification.adapter.out.jpa.PlatformType;
import lombok.Builder;

@Builder
public record FcmTokenRegisterCommand(
        String deviceId,
        String fcmToken,
        PlatformType platformType,
        Long userId
) {
}
