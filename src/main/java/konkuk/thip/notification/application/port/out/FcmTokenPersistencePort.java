package konkuk.thip.notification.application.port.out;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.notification.domain.FcmToken;

import java.util.List;
import java.util.Optional;

public interface FcmTokenPersistencePort {
    Optional<FcmToken> findByDeviceId(String deviceId);

    default FcmToken getByDeviceIdOrThrow(String deviceId) {
        return findByDeviceId(deviceId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FCM_TOKEN_NOT_FOUND));
    }

    FcmToken save(FcmToken token);

    void update(FcmToken fcmToken);

    List<FcmToken> findEnabledByUserId(Long userId);

    void deleteByUserIdAndDeviceId(Long userId, String deviceId);
}
