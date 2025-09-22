package konkuk.thip.notification.application.service.template.room;

import konkuk.thip.notification.application.service.template.NotificationTemplate;
import konkuk.thip.notification.domain.value.NotificationCategory;

public enum RoomPostLikedTemplate implements NotificationTemplate<RoomPostLikedTemplate.Args> {
    INSTANCE;

    @Override
    public String title(Args args) {
        return NotificationCategory.ROOM.prefixedTitle("좋아요 알림");
    }

    @Override
    public String content(Args args) {
        return "@" + args.actorUsername() + " 님이 내 독서기록에 좋아요를 눌렀어요!";
    }

    @Override
    public NotificationCategory notificationCategory(Args args) {
        return NotificationCategory.ROOM;
    }

    public record Args(String actorUsername) {}
}
