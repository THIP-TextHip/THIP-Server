package konkuk.thip.notification.application.port.in;

import konkuk.thip.notification.adapter.in.web.response.NotificationShowResponse;
import konkuk.thip.notification.application.port.in.dto.NotificationType;

public interface NotificationShowUseCase {

    NotificationShowResponse showNotifications(Long userId, String cursorStr, NotificationType notificationType);
}
