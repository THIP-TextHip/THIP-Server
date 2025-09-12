package konkuk.thip.user.application.service;

import konkuk.thip.notification.application.port.out.FcmTokenPersistencePort;
import konkuk.thip.notification.domain.FcmToken;
import konkuk.thip.user.application.port.in.UserShowNotificationSettingsInfoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserShowNotificationSettingInfoService implements UserShowNotificationSettingsInfoUseCase {

    private final FcmTokenPersistencePort fcmTokenPersistencePort;

    @Override
    @Transactional(readOnly = true)
    public boolean getUserNotificationSettingsInfo(Long userId,String deviceId) {

        FcmToken fcmToken = fcmTokenPersistencePort.getByDeviceIdOrThrow(deviceId);

        fcmToken.validateFcmOwner(userId);

        return fcmToken.isEnabled();
    }
}
