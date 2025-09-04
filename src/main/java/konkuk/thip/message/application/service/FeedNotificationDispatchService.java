package konkuk.thip.message.application.service;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import konkuk.thip.message.application.port.in.FeedNotificationDispatchUseCase;
import konkuk.thip.message.application.port.out.FirebaseMessagingPort;
import konkuk.thip.message.adapter.out.event.dto.FeedEvents;
import konkuk.thip.message.domain.NotificationCategory;
import konkuk.thip.message.domain.MessageRoute;
import konkuk.thip.notification.application.port.out.FcmTokenLoadPort;
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

    private final FcmTokenLoadPort fcmTokenLoadPort;
    private final FirebaseMessagingPort firebasePort;

    @Override
    public void handleFollower(final FeedEvents.FollowerEvent event) {
        Notification n = buildNotification("팔로워 알림",
                "@" + event.actorUsername() + " 님이 나를 띱했어요!");

        List<FcmToken> tokens = fcmTokenLoadPort.findEnabledByUserId(event.targetUserId());

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
        Notification notification = buildNotification("새로운 댓글이 달렸어요",
                "@" +event.actorUsername() + " 님이 내 글에 댓글을 달았어요!");

        pushFeedDetail(event.targetUserId(), notification, event.feedId());
    }

    @Override
    public void handleFeedCommentReplied(final FeedEvents.FeedCommentRepliedEvent event) {
        Notification notification = buildNotification("새로운 답글이 달렸어요",
                "@" + event.actorUsername() + " 님이 내 댓글에 답글을 달았어요!");

        pushFeedDetail(event.targetUserId(), notification, event.feedId());
    }

    @Override
    public void handleFolloweeNewPost(final FeedEvents.FolloweeNewPostEvent event) {
        Notification notification = buildNotification("새 글 알림",
                "@" + event.actorUsername() + " 님이 새로운 글을 작성했어요!");

        pushFeedDetail(event.targetUserId(), notification, event.feedId());
    }

    @Override
    public void handleFeedLiked(final FeedEvents.FeedLikedEvent event) {
        Notification notification = buildNotification("내 글을 좋아합니다",
                "@" + event.actorUsername() + " 님이 내 글에 좋아요를 눌렀어요!");

        pushFeedDetail(event.targetUserId(), notification, event.feedId());
    }

    @Override
    public void handleFeedCommentLiked(final FeedEvents.FeedCommentLikedEvent event) {
        Notification notification = buildNotification("좋아요 알림",
                "@" + event.actorUsername() + " 님이 내 댓글에 좋아요를 눌렀어요!");

        pushFeedDetail(event.targetUserId(), notification, event.feedId());
    }

    private void pushFeedDetail(Long userId, Notification notification, Long feedId) {
        List<FcmToken> tokens = fcmTokenLoadPort.findEnabledByUserId(userId);

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
        return Notification.builder().setTitle(NotificationCategory.FEED.prefixedTitle(title)).setBody(body).build();
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