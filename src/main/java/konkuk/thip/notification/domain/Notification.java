package konkuk.thip.notification.domain;

import konkuk.thip.common.entity.BaseDomainEntity;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.notification.domain.value.NotificationCategory;
import konkuk.thip.notification.domain.value.NotificationRedirectSpec;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import static konkuk.thip.common.exception.code.ErrorCode.NOTIFICATION_ACCESS_FORBIDDEN;
import static konkuk.thip.common.exception.code.ErrorCode.NOTIFICATION_ALREADY_CHECKED;

@Getter
@SuperBuilder
public class Notification extends BaseDomainEntity {

    private Long id;

    private String title;

    private String content;

    private boolean isChecked;

    private NotificationCategory notificationCategory;

    private Long targetUserId;

    private NotificationRedirectSpec redirectSpec;

    public static Notification withoutId(String title, String content, NotificationCategory notificationCategory, Long targetUserId,
                                          NotificationRedirectSpec redirectSpec) {
        return Notification.builder()
                .title(title)
                .content(content)
                .isChecked(false)
                .notificationCategory(notificationCategory)
                .targetUserId(targetUserId)
                .redirectSpec(redirectSpec)
                .build();
    }

    public void validateOwner(Long userId) {
        if (!targetUserId.equals(userId)) {
            throw new InvalidStateException(NOTIFICATION_ACCESS_FORBIDDEN);
        }
    }

    public void markToChecked() {
        if (isChecked) {
            throw new InvalidStateException(NOTIFICATION_ALREADY_CHECKED);
        }

        this.isChecked = true;
    }
}
