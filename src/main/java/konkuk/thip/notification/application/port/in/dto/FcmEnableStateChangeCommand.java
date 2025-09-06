package konkuk.thip.notification.application.port.in.dto;

public record FcmEnableStateChangeCommand(
        Long userId,
        boolean enable,
        String deviceId
) {
}
