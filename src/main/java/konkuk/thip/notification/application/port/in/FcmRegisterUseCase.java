package konkuk.thip.notification.application.port.in;

import konkuk.thip.notification.application.port.in.dto.FcmTokenRegisterCommand;

public interface FcmRegisterUseCase {
    void registerToken(FcmTokenRegisterCommand command);
}
