package konkuk.thip.notification.adapter.out.persistence.function;

import konkuk.thip.notification.application.port.out.dto.NotificationQueryDto;

import java.util.List;

@FunctionalInterface
public interface PrimaryKeyNotificationQueryFunction {

    List<NotificationQueryDto> apply(Long lastNotificationId, int pageSize);
}
