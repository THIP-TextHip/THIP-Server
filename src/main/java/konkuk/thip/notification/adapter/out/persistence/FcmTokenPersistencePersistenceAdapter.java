package konkuk.thip.notification.adapter.out.persistence;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.notification.adapter.out.jpa.FcmTokenJpaEntity;
import konkuk.thip.notification.adapter.out.mapper.FcmTokenMapper;
import konkuk.thip.notification.adapter.out.persistence.repository.FcmTokenJpaRepository;
import konkuk.thip.notification.application.port.out.FcmTokenPersistencePort;
import konkuk.thip.notification.domain.FcmToken;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FcmTokenPersistencePersistenceAdapter implements FcmTokenPersistencePort {

    private final FcmTokenJpaRepository fcmTokenJpaRepository;
    private final UserJpaRepository userJpaRepository;

    private final FcmTokenMapper fcmTokenMapper;

    @Override
    public Optional<FcmToken> findByDeviceId(String deviceId) {
        return fcmTokenJpaRepository.findByDeviceId(deviceId)
                .map(fcmTokenMapper::toDomainEntity);
    }

    @Override
    public FcmToken save(FcmToken token) {
        UserJpaEntity user = userJpaRepository.findByUserId(token.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        var saved = fcmTokenJpaRepository.save(fcmTokenMapper.toJpaEntity(token, user));
        return fcmTokenMapper.toDomainEntity(saved);
    }

    @Override
    public void update(FcmToken fcmToken) {
        UserJpaEntity userJpaEntity = userJpaRepository.findByUserId(fcmToken.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        FcmTokenJpaEntity fcmTokenJpaEntity = fcmTokenJpaRepository.findByFcmTokenId(fcmToken.getId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FCM_TOKEN_NOT_FOUND));

        fcmTokenJpaEntity.updateFrom(fcmToken, userJpaEntity);
        fcmTokenJpaRepository.save(fcmTokenJpaEntity);
    }

    @Override
    public List<FcmToken> findEnabledByUserId(Long userId) {
        return fcmTokenJpaRepository.findByUserIdAndIsEnabledTrue(userId).stream()
                .map(fcmTokenMapper::toDomainEntity).toList();
    }

    @Override
    public void deleteByUserIdAndDeviceId(Long userId, String deviceId) {
        fcmTokenJpaRepository.deleteByUserIdAndDeviceId(userId, deviceId);
    }
}
