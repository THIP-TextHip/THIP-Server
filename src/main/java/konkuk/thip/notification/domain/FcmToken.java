package konkuk.thip.notification.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.notification.domain.value.PlatformType;
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

    public void changeEnableState(boolean enable, long actorUserId) {
        validateChangeEnableState(enable, actorUserId);
        this.isEnabled = enable;
    }

    private void validateChangeEnableState(boolean enable, long actorUserId) {
        if (this.isEnabled == enable) {
            throw new InvalidStateException(ErrorCode.FCM_TOKEN_ENABLED_STATE_ALREADY,
                    new IllegalArgumentException("이미 " + (enable ? "활성화" : "비활성화") + "된 상태입니다."));
        }
        if (this.userId != actorUserId) {
            throw new InvalidStateException(ErrorCode.FCM_TOKEN_CHANGE_ENABLE_STATE_FORBIDDEN);
        }
    }
}
