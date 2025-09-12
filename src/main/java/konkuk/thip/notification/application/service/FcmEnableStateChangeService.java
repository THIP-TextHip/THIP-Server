package konkuk.thip.notification.application.service;

import konkuk.thip.notification.application.port.in.FcmEnableStateChangeUseCase;
import konkuk.thip.notification.application.port.in.dto.FcmEnableStateChangeCommand;
import konkuk.thip.notification.application.port.out.FcmTokenPersistencePort;
import konkuk.thip.notification.domain.FcmToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmEnableStateChangeService implements FcmEnableStateChangeUseCase {

    private final FcmTokenPersistencePort fcmTokenPersistencePort;

    @Override
    @Transactional
    public boolean changeEnableState(FcmEnableStateChangeCommand command) {
        FcmToken fcmToken = fcmTokenPersistencePort.getByDeviceIdOrThrow(command.deviceId());

        fcmToken.changeEnableState(command.enable(), command.userId());
        fcmTokenPersistencePort.update(fcmToken);

        return command.enable();
    }
}
