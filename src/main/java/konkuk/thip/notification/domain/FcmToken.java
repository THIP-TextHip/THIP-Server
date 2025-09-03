package konkuk.thip.notification.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.notification.adapter.out.jpa.PlatformType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@SuperBuilder
public class FcmToken extends BaseDomainEntity {

    private Long id;

    private String fcmToken;

    private String deviceId;

    private PlatformType platformType;

    private LocalDate lastUsedTime;

    private boolean isEnabled; // 푸쉬알림 수신 여부

    private Long userId;

    public static FcmToken withoutId (
            String fcmToken,
            String deviceId,
            PlatformType platformType,
            LocalDate lastUsedTime,
            boolean isEnabled,
            Long userId
    ) {
        return FcmToken.builder()
                .fcmToken(fcmToken)
                .deviceId(deviceId)
                .platformType(platformType)
                .lastUsedTime(lastUsedTime)
                .isEnabled(isEnabled)
                .userId(userId)
                .build();
    }

    // 토큰 갱신
    public void updateToken(String fcmToken, PlatformType platformType, LocalDate lastUsedTime, Long userId) {
        this.fcmToken = fcmToken;
        this.platformType = platformType;
        this.lastUsedTime = lastUsedTime;
        this.userId = userId;
    }
}
