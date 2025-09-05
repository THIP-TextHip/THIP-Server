package konkuk.thip.notification.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.notification.domain.FcmToken;
import konkuk.thip.notification.domain.value.PlatformType;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "fcm_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FcmTokenJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fcmTokenId;

    @Column(name = "fcm_token", nullable = false)
    private String fcmToken;

    @Column(name = "device_id", length = 128, nullable = false, unique = true)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", length = 16, nullable = false)
    private PlatformType platformType;

    @Column(name = "last_used_time", nullable = false)
    private LocalDate lastUsedTime;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled; // 푸쉬알림 수신 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity userJpaEntity;

    public void updateFrom(FcmToken fcmToken, UserJpaEntity userJpaEntity) {
        this.fcmToken = fcmToken.getFcmToken();
        this.platformType = fcmToken.getPlatformType();
        this.lastUsedTime = fcmToken.getLastUsedTime();
        this.userJpaEntity = userJpaEntity;

        this.isEnabled = fcmToken.isEnabled();
    }
}
