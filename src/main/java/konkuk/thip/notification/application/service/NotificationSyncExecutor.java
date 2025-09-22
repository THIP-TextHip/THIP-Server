package konkuk.thip.notification.application.service;

import konkuk.thip.common.annotation.application.HelperService;
import konkuk.thip.notification.application.port.out.NotificationCommandPort;
import konkuk.thip.notification.application.service.template.NotificationTemplate;
import konkuk.thip.notification.domain.Notification;
import konkuk.thip.notification.domain.value.NotificationCategory;
import konkuk.thip.notification.domain.value.NotificationRedirectSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@HelperService
@RequiredArgsConstructor
@Slf4j
public class NotificationSyncExecutor {

    private final NotificationCommandPort notificationCommandPort;

    /**
     * NotificationRedirectSpec 미지정 시
     */
    public <T> void execute(
            NotificationTemplate<T> template,
            T args,
            Long targetUserId,
            EventCommandInvoker invoker
    ) {
        execute(template, args, targetUserId, NotificationRedirectSpec.none(), invoker);
    }

    public <T> void execute(
            NotificationTemplate<T> template,
            T args,
            Long targetUserId,
            NotificationRedirectSpec notificationRedirectSpec,
            EventCommandInvoker invoker
    ) {
        String title = template.title(args);
        String content = template.content(args);
        NotificationCategory notificationCategory = template.notificationCategory(args);

        // 1. DB 저장
        Long notificationId = saveNotification(title, content, notificationCategory, targetUserId, notificationRedirectSpec);

        // 2. 이벤트 퍼블리시
        try {
            invoker.publish(title, content, notificationId);
        } catch (Exception e) {
            // 이벤트 발행 실패 시, DB에 저장된 알림을 롤백하지는 않음
            // -> 알림 저장은 비즈니스 트랜잭션과 동일한 경계 내에서 수행되므로, 알림 저장은 유지
            // -> 푸시 알림 이벤트 발행이 실패한 경우, 일단 로깅만 추가
            log.error("푸시 알림 이벤트 퍼블리시 실패 targetUserId = {}, title = {}, notificationId = {}", targetUserId, title, notificationId, e);
        }
    }

    private Long saveNotification(String title, String content, NotificationCategory category, Long targetUserId, NotificationRedirectSpec redirectSpec) {
        Notification notification = Notification.withoutId(title, content, category, targetUserId, redirectSpec);
        return notificationCommandPort.save(notification);
    }
}
