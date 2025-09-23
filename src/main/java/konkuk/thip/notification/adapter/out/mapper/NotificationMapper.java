package konkuk.thip.notification.adapter.out.mapper;

import konkuk.thip.notification.adapter.out.jpa.NotificationJpaEntity;
import konkuk.thip.notification.domain.Notification;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationJpaEntity toJpaEntity(Notification notification, UserJpaEntity userJpaEntity) {
        return NotificationJpaEntity.builder()
                .title(notification.getTitle())
                .content(notification.getContent())
                .isChecked(notification.isChecked())
                .notificationCategory(notification.getNotificationCategory())
                .userJpaEntity(userJpaEntity)
                .redirectSpec(notification.getRedirectSpec())
                .build();
    }

    public Notification toDomainEntity(NotificationJpaEntity notificationJpaEntity) {
        return Notification.builder()
                .id(notificationJpaEntity.getNotificationId())
                .title(notificationJpaEntity.getTitle())
                .content(notificationJpaEntity.getContent())
                .isChecked(notificationJpaEntity.isChecked())
                .notificationCategory(notificationJpaEntity.getNotificationCategory())
                .targetUserId(notificationJpaEntity.getUserJpaEntity().getUserId())
                .redirectSpec(notificationJpaEntity.getRedirectSpec())
                .createdAt(notificationJpaEntity.getCreatedAt())
                .modifiedAt(notificationJpaEntity.getModifiedAt())
                .status(notificationJpaEntity.getStatus())
                .build();
    }
}
