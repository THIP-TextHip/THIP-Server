package konkuk.thip.notification.application.service.template.room;

import konkuk.thip.notification.application.service.template.NotificationTemplate;
import konkuk.thip.notification.domain.value.NotificationCategory;

public enum RoomPostCommentRepliedTemplate implements NotificationTemplate<RoomPostCommentRepliedTemplate.Args> {
    INSTANCE;

    @Override
    public String title(Args args) {
        return NotificationCategory.ROOM.prefixedTitle("새로운 답글이 달렸어요");
    }

    @Override
    public String content(Args args) {
        return "@" + args.actorUsername() + " 님이 내 댓글에 답글을 달았어요!";
    }

    @Override
    public NotificationCategory notificationCategory(Args args) {
        return NotificationCategory.ROOM;
    }

    public record Args(String actorUsername) {}
}
