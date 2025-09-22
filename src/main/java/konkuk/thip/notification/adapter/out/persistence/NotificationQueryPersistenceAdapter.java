package konkuk.thip.notification.adapter.out.persistence;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.notification.adapter.out.mapper.NotificationMapper;
import konkuk.thip.notification.adapter.out.persistence.function.PrimaryKeyNotificationQueryFunction;
import konkuk.thip.notification.adapter.out.persistence.repository.NotificationJpaRepository;
import konkuk.thip.notification.application.port.out.NotificationQueryPort;
import konkuk.thip.notification.application.port.out.dto.NotificationQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationQueryPersistenceAdapter implements NotificationQueryPort {

    private final NotificationJpaRepository notificationJpaRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public CursorBasedList<NotificationQueryDto> findFeedNotificationsByUserId(Long userId, Cursor cursor) {
        return findNotificationsByPrimaryKeyCursor(cursor, ((lastNotificationId, pageSize) ->
                notificationJpaRepository.findFeedNotificationsOrderByCreatedAtDesc(userId, lastNotificationId, pageSize)
        ));
    }

    @Override
    public CursorBasedList<NotificationQueryDto> findRoomNotificationsByUserId(Long userId, Cursor cursor) {
        return findNotificationsByPrimaryKeyCursor(cursor, ((lastNotificationId, pageSize) ->
                notificationJpaRepository.findRoomNotificationsOrderByCreatedAtDesc(userId, lastNotificationId, pageSize)
        ));
    }

    @Override
    public CursorBasedList<NotificationQueryDto> findFeedAndRoomNotificationsByUserId(Long userId, Cursor cursor) {
        return findNotificationsByPrimaryKeyCursor(cursor, ((lastNotificationId, pageSize) ->
                notificationJpaRepository.findFeedAndRoomNotificationsOrderByCreatedAtDesc(userId, lastNotificationId, pageSize)
        ));
    }

    private CursorBasedList<NotificationQueryDto> findNotificationsByPrimaryKeyCursor(Cursor cursor, PrimaryKeyNotificationQueryFunction queryFunction) {
        Long lastNotificationId = cursor.isFirstRequest() ? null : cursor.getLong(0);
        int pageSize = cursor.getPageSize();

        List<NotificationQueryDto> dtos = queryFunction.apply(lastNotificationId, pageSize);

        return CursorBasedList.of(dtos, pageSize, dto -> {
            Cursor nextCursor = new Cursor(List.of(
                    dto.notificationId().toString()
            ));
            return nextCursor.toEncodedString();
        });
    }
}
