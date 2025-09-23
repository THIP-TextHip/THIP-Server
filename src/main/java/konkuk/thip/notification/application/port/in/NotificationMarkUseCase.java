package konkuk.thip.notification.application.port.in;

import konkuk.thip.notification.adapter.in.web.response.NotificationMarkToCheckedResponse;

public interface NotificationMarkUseCase {

    NotificationMarkToCheckedResponse markToChecked(Long notificationId, Long userId);
}
