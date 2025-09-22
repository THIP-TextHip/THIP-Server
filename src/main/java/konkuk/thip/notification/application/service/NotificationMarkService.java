package konkuk.thip.notification.application.service;

import konkuk.thip.notification.adapter.in.web.response.NotificationMarkToCheckedResponse;
import konkuk.thip.notification.application.port.in.NotificationMarkUseCase;
import konkuk.thip.notification.application.port.out.NotificationCommandPort;
import konkuk.thip.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationMarkService implements NotificationMarkUseCase {

    private final NotificationCommandPort notificationCommandPort;

    @Override
    @Transactional
    public NotificationMarkToCheckedResponse markToChecked(Long notificationId, Long userId) {
        // 1. 알림 존재 여부 확인
        Notification notification = notificationCommandPort.getByIdOrThrow(notificationId);
        notification.validateOwner(userId);

        // 2. 알림 읽음 처리
        notification.markToChecked();
        notificationCommandPort.update(notification);

        // 3. 읽음 처리된 알림의 redirectSpec 반환 (for FE 알림 리다이렉트 동작)
        return new NotificationMarkToCheckedResponse(
                notification.getRedirectSpec().route(),
                notification.getRedirectSpec().params()
        );
    }
}
