package konkuk.thip.notification.adapter.in.web.request;

import konkuk.thip.notification.application.port.in.dto.FcmTokenDeleteCommand;

public record FcmTokenDeleteRequest(
        String deviceId
) {
    public FcmTokenDeleteCommand toCommand(Long userId) {
        return new FcmTokenDeleteCommand(
                userId,
                this.deviceId
        );
    }
}
