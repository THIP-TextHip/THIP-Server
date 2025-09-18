package konkuk.thip.notification.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.notification.domain.value.NotificationCategory;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Notification extends BaseDomainEntity {

    private Long id;

    private String title;

    private String content;

    private boolean isChecked;

    private NotificationCategory notificationCategory;

    private Long targetUserId;

    public static Notification withoutId (String title, String content, NotificationCategory notificationCategory, Long targetUserId) {
        return Notification.builder()
                .title(title)
                .content(content)
                .isChecked(false)
                .notificationCategory(notificationCategory)
                .targetUserId(targetUserId)
                .build();
    }
}
