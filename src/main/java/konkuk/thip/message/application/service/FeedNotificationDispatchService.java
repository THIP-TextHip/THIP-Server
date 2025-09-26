package konkuk.thip.message.application.service;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import konkuk.thip.message.application.port.in.FeedNotificationDispatchUseCase;
import konkuk.thip.message.application.port.out.FirebaseMessagingPort;
import konkuk.thip.message.adapter.out.event.dto.FeedEvents;
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
public class FeedNotificationDispatchService implements FeedNotificationDispatchUseCase {

    private final FcmTokenPersistencePort fcmTokenPersistencePort;
    private final FirebaseMessagingPort firebasePort;

    @Override
    public void handleFollower(final FeedEvents.FollowerEvent event) {
        Notification n = buildFcmNotification(event.title(), event.content());
        push(event.targetUserId(), n, event.notificationId());
    }

    @Override
    public void handleFeedCommented(final FeedEvents.FeedCommentedEvent event) {
        Notification n = buildFcmNotification(event.title(), event.content());
        push(event.targetUserId(), n, event.notificationId());
    }

    @Override
    public void handleFeedCommentReplied(final FeedEvents.FeedCommentRepliedEvent event) {
        Notification n = buildFcmNotification(event.title(), event.content());
        push(event.targetUserId(), n, event.notificationId());
    }

    @Override
    public void handleFolloweeNewFeed(final FeedEvents.FolloweeNewFeedEvent event) {
        Notification n = buildFcmNotification(event.title(), event.content());
        push(event.targetUserId(), n, event.notificationId());
    }

    @Override
    public void handleFeedLiked(final FeedEvents.FeedLikedEvent event) {
        Notification n = buildFcmNotification(event.title(), event.content());
        push(event.targetUserId(), n, event.notificationId());
    }

    @Override
    public void handleFeedCommentLiked(final FeedEvents.FeedCommentLikedEvent event) {
        Notification n = buildFcmNotification(event.title(), event.content());
        push(event.targetUserId(), n, event.notificationId());
    }

    private void push(Long userId, Notification n, Long notificationId) {
        List<FcmToken> tokens = fcmTokenPersistencePort.findEnabledByUserId(userId);
        if (tokens.isEmpty()) return;

        List<Message> msgs = new ArrayList<>(tokens.size());
        List<String> tk  = new ArrayList<>(tokens.size());
        List<String> dev = new ArrayList<>(tokens.size());

        for (FcmToken t : tokens) {
            Message m = Message.builder()
                    .setToken(t.getFcmToken())
                    .setNotification(n)
                    .putData("category", NotificationCategory.FEED.getDisplay())
                    .putData("action", "OPEN_NOTIFICATION") // FE는 이 액션으로 알림 상세/라우팅을 BE api 요청으로 처리
                    .putData("notificationId", String.valueOf(notificationId))
                    .build();

            msgs.add(m);
            tk.add(t.getFcmToken());
            dev.add(t.getDeviceId());
        }

        firebasePort.sendBatch(msgs, tk, dev);
    }

    private Notification buildFcmNotification(final String title, final String body) {
        return Notification.builder().setTitle(title).setBody(body).build();
    }
}
