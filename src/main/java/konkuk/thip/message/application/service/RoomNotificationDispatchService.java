package konkuk.thip.message.application.service;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import konkuk.thip.message.application.port.in.RoomNotificationDispatchUseCase;
import konkuk.thip.message.application.port.out.FirebaseMessagingPort;
import konkuk.thip.message.adapter.out.event.dto.RoomEvents;
import konkuk.thip.notification.domain.value.NotificationCategory;
import konkuk.thip.notification.application.port.out.FcmTokenPersistencePort;
import konkuk.thip.notification.domain.FcmToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomNotificationDispatchService implements RoomNotificationDispatchUseCase {

    private final FcmTokenPersistencePort fcmTokenQueryPort;
    private final FirebaseMessagingPort firebasePort;

    @Override
    public void handleRoomPostCommented(final RoomEvents.RoomPostCommentedEvent e) {
        push(e.targetUserId(), e.title(), e.content(), e.notificationId());
    }

    @Override
    public void handleRoomVoteStarted(final RoomEvents.RoomVoteStartedEvent e) {
        push(e.targetUserId(), e.title(), e.content(), e.notificationId());
    }

    @Override
    public void handleRoomRecordCreated(final RoomEvents.RoomRecordCreatedEvent e) {
        push(e.targetUserId(), e.title(), e.content(), e.notificationId());
    }

    @Override
    public void handleRoomRecruitClosedEarly(final RoomEvents.RoomRecruitClosedEarlyEvent e) {
        push(e.targetUserId(), e.title(), e.content(), e.notificationId());
    }

    @Override
    public void handleRoomActivityStarted(final RoomEvents.RoomActivityStartedEvent e) {
        push(e.targetUserId(), e.title(), e.content(), e.notificationId());
    }

    @Override
    public void handleRoomJoinRequestedToOwner(final RoomEvents.RoomJoinRequestedToOwnerEvent e) {
        push(e.targetUserId(), e.title(), e.content(), e.notificationId());
    }

    @Override
    public void handleRoomCommentLiked(final RoomEvents.RoomCommentLikedEvent e) {
        push(e.targetUserId(), e.title(), e.content(), e.notificationId());
    }

    @Override
    public void handleRoomPostLiked(final RoomEvents.RoomPostLikedEvent e) {
        push(e.targetUserId(), e.title(), e.content(), e.notificationId());
    }

    @Override
    public void handleRoomPostCommentReplied(final RoomEvents.RoomPostCommentRepliedEvent e) {
        push(e.targetUserId(), e.title(), e.content(), e.notificationId());
    }

    // ===== helpers =====
    private void push(Long userId, String title, String content, Long notificationId) {
        Notification notification = buildNotification(title, content);

        List<FcmToken> tokens = fcmTokenQueryPort.findEnabledByUserId(userId);
        if (tokens.isEmpty()) return;

        List<Message> msgs = new ArrayList<>(tokens.size());
        List<String> tk  = new ArrayList<>(tokens.size());
        List<String> dev = new ArrayList<>(tokens.size());

        for (FcmToken t : tokens) {
            Message m = Message.builder()
                    .setToken(t.getFcmToken())
                    .setNotification(notification)
                    .putData("category", NotificationCategory.ROOM.getDisplay())
                    .putData("action", "OPEN_NOTIFICATION") // FE는 이 액션으로 알림 상세/라우팅을 BE api 요청으로 처리
                    .putData("notificationId", String.valueOf(notificationId))
                    .build();
            msgs.add(m); tk.add(t.getFcmToken()); dev.add(t.getDeviceId());
        }

        firebasePort.sendBatch(msgs, tk, dev);
    }

    private Notification buildNotification(final String title, final String body) {
        return Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();
    }
}
