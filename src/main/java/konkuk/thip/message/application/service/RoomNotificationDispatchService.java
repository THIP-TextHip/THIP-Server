package konkuk.thip.message.application.service;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import konkuk.thip.message.application.port.in.RoomNotificationDispatchUseCase;
import konkuk.thip.message.application.port.out.FirebaseMessagingPort;
import konkuk.thip.message.adapter.out.event.dto.RoomEvents;
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
public class RoomNotificationDispatchService implements RoomNotificationDispatchUseCase {

    private final FcmTokenLoadPort fcmTokenQueryPort;
    private final FirebaseMessagingPort firebasePort;

    @Override
    public void handleRoomPostCommented(final RoomEvents.RoomPostCommentedEvent event) {
        Notification notification = buildNotification("새로운 댓글이 달렸어요",
                "@" + event.actorUsername() + " 님이 내 독서기록에 댓글을 달았어요!");

        List<FcmToken> tokens = fcmTokenQueryPort.findEnabledByUserId(event.targetUserId());
        if (tokens.isEmpty()) return;

        List<Message> msgs = new ArrayList<>(tokens.size());
        List<String> tk  = new ArrayList<>(tokens.size());
        List<String> dev = new ArrayList<>(tokens.size());

        for (FcmToken t : tokens) {
            Message m = buildMessage(t.getFcmToken(), notification,
                    MessageRoute.ROOM_RECORD_DETAIL,
                    "roomId", String.valueOf(event.roomId()),
                    "page", String.valueOf(event.page()),
                    "type", "group",
                    "postId", String.valueOf(event.postId()),
                    "postType", event.postType());

            msgs.add(m); tk.add(t.getFcmToken()); dev.add(t.getDeviceId());
        }
        firebasePort.sendBatch(msgs, tk, dev);
    }

    @Override
    public void handleRoomVoteStarted(final RoomEvents.RoomVoteStartedEvent event) {
        Notification notification = buildNotification(event.roomTitle(),
                "새로운 투표가 시작되었어요!");

        List<FcmToken> tokens = fcmTokenQueryPort.findEnabledByUserId(event.targetUserId());
        if (tokens.isEmpty()) return;

        List<Message> msgs = new ArrayList<>(tokens.size());
        List<String> tk  = new ArrayList<>(tokens.size());
        List<String> dev = new ArrayList<>(tokens.size());

        for (FcmToken t : tokens) {
            Message m = buildMessage(t.getFcmToken(), notification,
                    MessageRoute.ROOM_VOTE_DETAIL,
                    "roomId", String.valueOf(event.roomId()),
                    "page", String.valueOf(event.page()),
                    "type", "group",
                    "postId", String.valueOf(event.postId()),
                    "postType", "VOTE");

            msgs.add(m); tk.add(t.getFcmToken()); dev.add(t.getDeviceId());
        }
        firebasePort.sendBatch(msgs, tk, dev);
    }

    @Override
    public void handleRoomRecordCreated(final RoomEvents.RoomRecordCreatedEvent event) {
        Notification notification = buildNotification(event.roomTitle(),
                "@" + event.actorUsername() + " 님이 새로운 독서 기록을 작성했어요!");

        List<FcmToken> tokens = fcmTokenQueryPort.findEnabledByUserId(event.targetUserId());
        if (tokens.isEmpty()) return;

        List<Message> msgs = new ArrayList<>(tokens.size());
        List<String> tk  = new ArrayList<>(tokens.size());
        List<String> dev = new ArrayList<>(tokens.size());

        for (FcmToken t : tokens) {
            Message m = buildMessage(t.getFcmToken(), notification,
                    MessageRoute.ROOM_RECORD_DETAIL,
                    "roomId", String.valueOf(event.roomId()),
                    "page", String.valueOf(event.page()),
                    "type", "group",
                    "postId", String.valueOf(event.postId()),
                    "postType", "RECORD");

            msgs.add(m); tk.add(t.getFcmToken()); dev.add(t.getDeviceId());
        }
        firebasePort.sendBatch(msgs, tk, dev);
    }

    @Override
    public void handleRoomRecruitClosedEarly(final RoomEvents.RoomRecruitClosedEarlyEvent event) {
        Notification n = buildNotification(event.roomTitle(),
                "모임방 활동이 시작되었어요. 모임방에서 독서 기록을 시작해보세요!");

        pushRoomMain(event.targetUserId(), event.roomId(), n);
    }

    @Override
    public void handleRoomActivityStarted(final RoomEvents.RoomActivityStartedEvent event) {
        Notification notification = buildNotification(event.roomTitle(),
                "모임방 활동이 시작되었어요. 모임방에서 독서 기록을 시작해보세요!");

        pushRoomMain(event.targetUserId(), event.roomId(), notification);
    }

    @Override
    public void handleRoomJoinRequestedToOwner(final RoomEvents.RoomJoinRequestedToOwnerEvent event) {
        Notification n = buildNotification(event.roomTitle(),
                "@" + event.applicantUsername() + " 님이 모임에 참여했어요!");

        pushRoomDetail(event.ownerUserId(), event.roomId(), n);
    }

    @Override
    public void handleRoomCommentLiked(final RoomEvents.RoomCommentLikedEvent event) {
        Notification notification = buildNotification("내 댓글을 좋아합니다",
                "@" + event.actorUsername() + " 님이 내 댓글에 좋아요를 눌렀어요!");

        List<FcmToken> tokens = fcmTokenQueryPort.findEnabledByUserId(event.targetUserId());
        if (tokens.isEmpty()) return;

        List<Message> msgs = new ArrayList<>(tokens.size());
        List<String> tk  = new ArrayList<>(tokens.size());
        List<String> dev = new ArrayList<>(tokens.size());

        for (FcmToken t : tokens) {
            Message m = buildMessage(t.getFcmToken(), notification,
                    MessageRoute.ROOM_RECORD_DETAIL,
                    "roomId", String.valueOf(event.roomId()),
                    "page", String.valueOf(event.page()),
                    "type", "group",
                    "postId", String.valueOf(event.postId()),
                    "postType", "RECORD");
            msgs.add(m); tk.add(t.getFcmToken()); dev.add(t.getDeviceId());
        }
        firebasePort.sendBatch(msgs, tk, dev);
    }

    @Override
    public void handleRoomPostLiked(final RoomEvents.RoomPostLikedEvent event) {
        Notification notification = buildNotification("좋아요 알림",
                "@" + event.actorUsername() + " 님이 내 독서기록에 좋아요를 눌렀어요!");

        List<FcmToken> tokens = fcmTokenQueryPort.findEnabledByUserId(event.targetUserId());
        if (tokens.isEmpty()) return;

        List<Message> msgs = new ArrayList<>(tokens.size());
        List<String> tk  = new ArrayList<>(tokens.size());
        List<String> dev = new ArrayList<>(tokens.size());

        for (FcmToken t : tokens) {
            Message m = buildMessage(t.getFcmToken(), notification,
                    MessageRoute.ROOM_RECORD_DETAIL,
                    "roomId", String.valueOf(event.roomId()),
                    "page", String.valueOf(event.page()),
                    "type", "group",
                    "postId", String.valueOf(event.postId()),
                    "postType", "RECORD");
            msgs.add(m); tk.add(t.getFcmToken()); dev.add(t.getDeviceId());
        }
        firebasePort.sendBatch(msgs, tk, dev);
    }

    @Override
    public void handleRoomPostCommentReplied(RoomEvents.RoomPostCommentRepliedEvent e) {
        Notification notification = buildNotification("새로운 댓글이 달렸어요",
                "@" + e.actorUsername() + " 님이 내 댓글에 대댓글을 달았어요!");

        List<FcmToken> tokens = fcmTokenQueryPort.findEnabledByUserId(e.targetUserId());
        if (tokens.isEmpty()) return;

        List<Message> msgs = new ArrayList<>(tokens.size());
        List<String> tk  = new ArrayList<>(tokens.size());
        List<String> dev = new ArrayList<>(tokens.size());

        for (FcmToken t : tokens) {
            Message m = buildMessage(t.getFcmToken(), notification,
                    MessageRoute.ROOM_RECORD_DETAIL,
                    "roomId", String.valueOf(e.roomId()),
                    "page", String.valueOf(e.page()),
                    "type", "group",
                    "postId", String.valueOf(e.postId()),
                    "postType", e.postType());

            msgs.add(m); tk.add(t.getFcmToken()); dev.add(t.getDeviceId());
        }
        firebasePort.sendBatch(msgs, tk, dev);
    }

    // ===== helpers =====

    private void pushRoomMain(Long targetUserId, Long roomId, Notification notification) {
        List<FcmToken> tokens = fcmTokenQueryPort.findEnabledByUserId(targetUserId);

        if (tokens.isEmpty()) return;

        List<Message> msgs = new ArrayList<>(tokens.size());
        List<String> tk  = new ArrayList<>(tokens.size());
        List<String> dev = new ArrayList<>(tokens.size());

        for (FcmToken t : tokens) {
            Message m = buildMessage(t.getFcmToken(), notification,
                    MessageRoute.ROOM_MAIN,
                    "roomId", String.valueOf(roomId));

            msgs.add(m); tk.add(t.getFcmToken()); dev.add(t.getDeviceId());
        }
        firebasePort.sendBatch(msgs, tk, dev);
    }

    private void pushRoomDetail(Long targetUserId, Long roomId, Notification notification) {
        List<FcmToken> tokens = fcmTokenQueryPort.findEnabledByUserId(targetUserId);

        if (tokens.isEmpty()) return;

        List<Message> msgs = new ArrayList<>(tokens.size());
        List<String> tk  = new ArrayList<>(tokens.size());
        List<String> dev = new ArrayList<>(tokens.size());

        for (FcmToken t : tokens) {
            Message m = buildMessage(t.getFcmToken(), notification,
                    MessageRoute.ROOM_DETAIL,
                    "roomId", String.valueOf(roomId));

            msgs.add(m); tk.add(t.getFcmToken()); dev.add(t.getDeviceId());
        }
        firebasePort.sendBatch(msgs, tk, dev);
    }

    private Notification buildNotification(final String title, final String body) {
        return Notification.builder().setTitle(NotificationCategory.ROOM.prefixedTitle(title)).setBody(body).build();
    }

    private Message buildMessage(final String token, final Notification n,
                                 final MessageRoute route,
                                 final String... kv) {
        var b = Message.builder()
                .setToken(token)
                .setNotification(n)
                .putData("category", NotificationCategory.ROOM.getDisplay())
                .putData("action", "OPEN_ROUTE")
                .putData("route", route.getCode());
        for (int i = 0; i + 1 < kv.length; i += 2) b.putData(kv[i], kv[i + 1]);
        return b.build();
    }
}