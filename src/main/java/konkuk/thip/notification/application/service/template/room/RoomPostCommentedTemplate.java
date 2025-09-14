package konkuk.thip.notification.application.service.template.room;

import konkuk.thip.notification.application.service.template.NotificationTemplate;
import konkuk.thip.notification.domain.value.NotificationCategory;

public enum RoomPostCommentedTemplate implements NotificationTemplate<RoomPostCommentedTemplate.Args> {
    INSTANCE;

    @Override
    public String title(Args args) {
        return NotificationCategory.ROOM.prefixedTitle("새로운 댓글이 달렸어요");
    }

    @Override
    public String content(Args args) {
        return "@" + args.actorUsername() + " 님이 내 독서기록에 댓글을 달았어요!";
    }

    public record Args(String actorUsername) {}
}
