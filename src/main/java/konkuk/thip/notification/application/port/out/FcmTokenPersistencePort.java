package konkuk.thip.notification.application.port.out;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.notification.domain.FcmToken;

import java.util.List;
import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.FCM_TOKEN_NOT_FOUND;

public interface FcmTokenPersistencePort {
    Optional<FcmToken> findByDeviceId(String deviceId);

    default FcmToken getByDeviceIdOrThrow(String deviceId) {
        return findByDeviceId(deviceId)
                .orElseThrow(() -> new EntityNotFoundException(FCM_TOKEN_NOT_FOUND));
    }

    Optional<FcmToken> findByDeviceIdAndUserId(String deviceId, Long userId);

    default FcmToken getByDeviceIdAndUserIdOrThrow(String deviceId, Long userId) {
        return findByDeviceIdAndUserId(deviceId, userId)
                .orElseThrow(() -> new EntityNotFoundException(FCM_TOKEN_NOT_FOUND));
    }

    FcmToken save(FcmToken token);

    void update(FcmToken fcmToken);

    List<FcmToken> findEnabledByUserId(Long userId);

    void deleteByUserIdAndDeviceId(Long userId, String deviceId);
}
