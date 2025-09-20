package konkuk.thip.notification.application.port.in.fcm;

import konkuk.thip.notification.application.port.in.dto.FcmTokenRegisterCommand;

public interface FcmRegisterUseCase {
    void registerToken(FcmTokenRegisterCommand command);
}
