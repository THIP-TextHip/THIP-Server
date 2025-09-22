package konkuk.thip.notification.application.port.in.fcm;

import konkuk.thip.notification.application.port.in.dto.FcmTokenDeleteCommand;

public interface FcmDeleteUseCase {
    void deleteToken(FcmTokenDeleteCommand command);
}
