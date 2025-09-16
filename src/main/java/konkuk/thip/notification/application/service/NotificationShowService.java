package konkuk.thip.notification.application.service;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.notification.adapter.in.web.response.NotificationShowResponse;
import konkuk.thip.notification.application.mapper.NotificationQueryMapper;
import konkuk.thip.notification.application.port.in.NotificationShowUseCase;
import konkuk.thip.notification.application.port.in.dto.NotificationType;
import konkuk.thip.notification.application.port.out.NotificationQueryPort;
import konkuk.thip.notification.application.port.out.dto.NotificationQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationShowService implements NotificationShowUseCase {

    private final static int PAGE_SIZE = 10;

    private final NotificationQueryPort notificationQueryPort;
    private final NotificationQueryMapper notificationQueryMapper;

    @Override
    @Transactional(readOnly = true)
    public NotificationShowResponse showNotifications(Long userId, String cursorStr, NotificationType notificationType) {
        // 1. Cursor 생성
        Cursor cursor = Cursor.from(cursorStr, PAGE_SIZE);

        // 2. 커서 기반 조회
        CursorBasedList<NotificationQueryDto> result = switch (notificationType) {
            case FEED -> notificationQueryPort.findFeedNotificationsByUserId(userId, cursor);
            case ROOM -> notificationQueryPort.findRoomNotificationsByUserId(userId, cursor);
            case FEED_AND_ROOM -> notificationQueryPort.findFeedAndRoomNotificationsByUserId(userId, cursor);
        };

        // 3. dto -> response 매핑
        var responses = notificationQueryMapper.toNotificationOfUsers(result.contents());

        return new NotificationShowResponse(responses, result.nextCursor(), !result.hasNext());
    }
}
