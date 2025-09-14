package konkuk.thip.message.application.service;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import konkuk.thip.message.application.port.in.FeedNotificationDispatchUseCase;
import konkuk.thip.message.application.port.out.FirebaseMessagingPort;
import konkuk.thip.message.adapter.out.event.dto.FeedEvents;
import konkuk.thip.notification.domain.value.NotificationCategory;
import konkuk.thip.message.domain.MessageRoute;
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
        Notification n = buildNotification(event.title(), event.content());

        List<FcmToken> tokens = fcmTokenPersistencePort.findEnabledByUserId(event.targetUserId());

        if (tokens.isEmpty()) return;

        List<Message> msgs = new ArrayList<>(tokens.size());
        List<String> tk  = new ArrayList<>(tokens.size());
        List<String> dev = new ArrayList<>(tokens.size());

        for (FcmToken t : tokens) {
            Message m = buildMessage(t.getFcmToken(), n,
                    MessageRoute.FEED_USER,
                    "userId", String.valueOf(event.actorUserId()));

            msgs.add(m); tk.add(t.getFcmToken()); dev.add(t.getDeviceId());
        }
        firebasePort.sendBatch(msgs, tk, dev);
    }

    @Override
    public void handleFeedCommented(final FeedEvents.FeedCommentedEvent event) {
        Notification notification = buildNotification(event.title(), event.content());

        pushFeedDetail(event.targetUserId(), notification, event.feedId());
    }

    @Override
    public void handleFeedCommentReplied(final FeedEvents.FeedCommentRepliedEvent event) {
        Notification notification = buildNotification(event.title(), event.content());

        pushFeedDetail(event.targetUserId(), notification, event.feedId());
    }

    @Override
    public void handleFolloweeNewPost(final FeedEvents.FolloweeNewPostEvent event) {
        Notification notification = buildNotification(event.title(), event.content());

        pushFeedDetail(event.targetUserId(), notification, event.feedId());
    }

    @Override
    public void handleFeedLiked(final FeedEvents.FeedLikedEvent event) {
        Notification notification = buildNotification(event.title(), event.content());

        pushFeedDetail(event.targetUserId(), notification, event.feedId());
    }

    @Override
    public void handleFeedCommentLiked(final FeedEvents.FeedCommentLikedEvent event) {
        Notification notification = buildNotification(event.title(), event.content());

        pushFeedDetail(event.targetUserId(), notification, event.feedId());
    }

    private void pushFeedDetail(Long userId, Notification notification, Long feedId) {
        List<FcmToken> tokens = fcmTokenPersistencePort.findEnabledByUserId(userId);

        if (tokens.isEmpty()) return;

        List<Message> msgs = new ArrayList<>(tokens.size());
        List<String> tk = new ArrayList<>(tokens.size());
        List<String> dev = new ArrayList<>(tokens.size());

        for (FcmToken t : tokens) {
            Message m = buildMessage(t.getFcmToken(), notification,
                    MessageRoute.FEED_DETAIL,
                    "feedId", String.valueOf(feedId));

            msgs.add(m);
            tk.add(t.getFcmToken());
            dev.add(t.getDeviceId());
        }
        firebasePort.sendBatch(msgs, tk, dev);
    }

    private Notification buildNotification(final String title, final String body) {
        return Notification.builder().setTitle(title).setBody(body).build();
    }

    private Message buildMessage(final String token, final Notification n,
                                 final MessageRoute route,
                                 final String... kv) {
        Message.Builder b = Message.builder()
                .setToken(token)
                .setNotification(n)
                .putData("category", NotificationCategory.FEED.getDisplay())
                .putData("action", "OPEN_ROUTE")
                .putData("route", route.getCode());
        for (int i = 0; i + 1 < kv.length; i += 2) b.putData(kv[i], kv[i + 1]);
        return b.build();
    }
}