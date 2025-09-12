package konkuk.thip.notification.application.service;

import konkuk.thip.notification.application.port.in.NotificationShowEnableStateUseCase;
import konkuk.thip.notification.application.port.out.FcmTokenPersistencePort;
import konkuk.thip.notification.domain.FcmToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationShowEnableStateService implements NotificationShowEnableStateUseCase {

    private final FcmTokenPersistencePort fcmTokenPersistencePort;

    @Override
    @Transactional(readOnly = true)
    public boolean getNotificationShowEnableState(Long userId, String deviceId) {

        FcmToken fcmToken = fcmTokenPersistencePort.getByDeviceIdAndUserIdOrThrow(deviceId, userId);

        return fcmToken.isEnabled();
    }
}
