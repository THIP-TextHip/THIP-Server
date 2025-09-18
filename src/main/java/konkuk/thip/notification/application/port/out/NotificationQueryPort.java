package konkuk.thip.notification.application.port.out;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.notification.application.port.out.dto.NotificationQueryDto;

public interface NotificationQueryPort {

    CursorBasedList<NotificationQueryDto> findFeedNotificationsByUserId(Long userId, Cursor cursor);

    CursorBasedList<NotificationQueryDto> findRoomNotificationsByUserId(Long userId, Cursor cursor);

    CursorBasedList<NotificationQueryDto> findFeedAndRoomNotificationsByUserId(Long userId, Cursor cursor);
}
