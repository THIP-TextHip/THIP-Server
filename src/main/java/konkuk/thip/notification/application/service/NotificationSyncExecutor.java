package konkuk.thip.notification.application.service;

import konkuk.thip.common.annotation.application.HelperService;
import konkuk.thip.notification.application.port.out.NotificationCommandPort;
import konkuk.thip.notification.application.service.template.NotificationTemplate;
import konkuk.thip.notification.domain.Notification;
import lombok.RequiredArgsConstructor;

@HelperService
@RequiredArgsConstructor
public class NotificationSyncExecutor {

    private final NotificationCommandPort notificationCommandPort;

    public  <T> void execute(
            NotificationTemplate<T> template,
            T args,
            Long targetUserId,
            EventCommandInvoker invoker
    ) {
        String title = template.title(args);
        String content = template.content(args);

        // 1. DB 저장
        saveNotification(title, content, targetUserId);

        // 2. 이벤트 퍼블리시
        invoker.publish(title, content);
    }

    private void saveNotification(String title, String content, Long targetUserId) {
        Notification notification = Notification.withoutId(title, content, targetUserId);
        notificationCommandPort.save(notification);
    }
}
