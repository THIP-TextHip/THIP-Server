package konkuk.thip.notification.adapter.out.persistence.repository;

import konkuk.thip.notification.application.port.out.dto.NotificationQueryDto;

import java.util.List;

public interface NotificationQueryRepository {

    List<NotificationQueryDto> findFeedNotificationsOrderByCreatedAtDesc(Long userId, Long lastNotificationId, int pageSize);

    List<NotificationQueryDto> findRoomNotificationsOrderByCreatedAtDesc(Long userId, Long lastNotificationId, int pageSize);

    List<NotificationQueryDto> findFeedAndRoomNotificationsOrderByCreatedAtDesc(Long userId, Long lastNotificationId, int pageSize);
}
