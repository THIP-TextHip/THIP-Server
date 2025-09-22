package konkuk.thip.notification.application.service.template;

import konkuk.thip.notification.domain.value.NotificationCategory;

public interface NotificationTemplate<T> {

    String title(T args);

    String content(T args);

    NotificationCategory notificationCategory(T args);
}
