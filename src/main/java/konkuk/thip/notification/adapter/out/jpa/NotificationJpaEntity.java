package konkuk.thip.notification.adapter.out.jpa;

import jakarta.persistence.*;
import konkuk.thip.common.entity.BaseJpaEntity;
import konkuk.thip.notification.domain.Notification;
import konkuk.thip.notification.domain.value.NotificationCategory;
import konkuk.thip.notification.domain.value.NotificationRedirectSpecConverter;
import konkuk.thip.notification.domain.value.NotificationRedirectSpec;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 200, nullable = false)
    private String content;

    @Column(name = "is_checked", nullable = false)
    private boolean isChecked;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_category", length = 16, nullable = false)
    private NotificationCategory notificationCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity userJpaEntity;

    @Convert(converter = NotificationRedirectSpecConverter.class)
    @Column(name = "redirect_spec", columnDefinition = "TEXT")  // nullable
    private NotificationRedirectSpec redirectSpec;

    public void updateFrom(Notification notification) {
        this.isChecked = notification.isChecked();  // 현재는 isChecked만 업데이트 가능
    }
}
