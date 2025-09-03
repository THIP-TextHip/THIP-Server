package konkuk.thip.notification.application.port.in.dto;

public record FcmTokenDeleteCommand(
        Long userId,
        String deviceId
) {
}
