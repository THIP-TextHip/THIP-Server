package konkuk.thip.notification.adapter.out.mapper;

import konkuk.thip.notification.adapter.out.jpa.FcmTokenJpaEntity;
import konkuk.thip.notification.domain.FcmToken;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class FcmTokenMapper {

    public FcmTokenJpaEntity toJpaEntity(FcmToken fcmToken, UserJpaEntity userJpaEntity) {
        return FcmTokenJpaEntity.builder()
                .fcmToken(fcmToken.getFcmToken())
                .deviceId(fcmToken.getDeviceId())
                .platformType(fcmToken.getPlatformType())
                .lastUsedTime(fcmToken.getLastUsedTime())
                .isEnabled(fcmToken.isEnabled())
                .userJpaEntity(userJpaEntity)
                .build();
    }

    public FcmToken toDomainEntity(FcmTokenJpaEntity fcmTokenJpaEntity) {
        return FcmToken.builder()
                .id(fcmTokenJpaEntity.getFcmTokenId())
                .fcmToken(fcmTokenJpaEntity.getFcmToken())
                .deviceId(fcmTokenJpaEntity.getDeviceId())
                .platformType(fcmTokenJpaEntity.getPlatformType())
                .lastUsedTime(fcmTokenJpaEntity.getLastUsedTime())
                .isEnabled(fcmTokenJpaEntity.isEnabled())
                .userId(fcmTokenJpaEntity.getUserJpaEntity().getUserId())
                .createdAt(fcmTokenJpaEntity.getCreatedAt())
                .modifiedAt(fcmTokenJpaEntity.getModifiedAt())
                .status(fcmTokenJpaEntity.getStatus())
                .build();
    }
}
