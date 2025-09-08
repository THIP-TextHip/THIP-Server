package konkuk.thip.notification.application.service;

import konkuk.thip.notification.application.port.in.FcmDeleteUseCase;
import konkuk.thip.notification.application.port.in.dto.FcmTokenDeleteCommand;
import konkuk.thip.notification.application.port.out.FcmTokenPersistencePort;
import konkuk.thip.notification.domain.FcmToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmDeleteService implements FcmDeleteUseCase {

    private final FcmTokenPersistencePort fcmTokenPersistencePort;

    @Override
    @Transactional
    public void deleteToken(FcmTokenDeleteCommand command) {
        FcmToken fcmToken = fcmTokenPersistencePort.getByDeviceIdOrThrow(command.deviceId());
        fcmToken.validateFcmOwner(command.userId());

        fcmTokenPersistencePort.deleteByUserIdAndDeviceId(command.userId(), command.deviceId());
    }
}
