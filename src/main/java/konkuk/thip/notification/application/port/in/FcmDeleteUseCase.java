package konkuk.thip.notification.application.port.in;

import konkuk.thip.notification.application.port.in.dto.FcmTokenDeleteCommand;

public interface FcmDeleteUseCase {
    void deleteToken(FcmTokenDeleteCommand command);
}
