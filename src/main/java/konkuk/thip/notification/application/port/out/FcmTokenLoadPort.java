package konkuk.thip.notification.application.port.out;

import konkuk.thip.notification.domain.FcmToken;

import java.util.List;
import java.util.Optional;

public interface FcmTokenLoadPort {
    Optional<FcmToken> findByDeviceId(String deviceId);

    FcmToken save(FcmToken token);

    void update(FcmToken fcmToken);

    List<FcmToken> findEnabledByUserId(Long userId);

    void disableById(Long id, String reason);
}
