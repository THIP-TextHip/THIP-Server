package konkuk.thip.user.adapter.out.mapper;

import konkuk.thip.user.adapter.out.jpa.NotificationJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.domain.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationJpaEntity toJpaEntity(Notification notification, UserJpaEntity userJpaEntity) {
        return NotificationJpaEntity.builder()
                .title(notification.getTitle())
                .content(notification.getContent())
                .isChecked(notification.isChecked())
                .userJpaEntity(userJpaEntity)
                .build();
    }

    public Notification toDomainEntity(NotificationJpaEntity notificationJpaEntity) {
        return Notification.builder()
                .id(notificationJpaEntity.getNotificationId())
                .title(notificationJpaEntity.getTitle())
                .content(notificationJpaEntity.getContent())
                .isChecked(notificationJpaEntity.isChecked())
                .targetUserId(notificationJpaEntity.getUserJpaEntity().getUserId())
                .createdAt(notificationJpaEntity.getCreatedAt())
                .modifiedAt(notificationJpaEntity.getModifiedAt())
                .status(notificationJpaEntity.getStatus())
                .build();
    }
}
