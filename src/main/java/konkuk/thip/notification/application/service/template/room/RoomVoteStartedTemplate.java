package konkuk.thip.notification.application.service.template.room;

import konkuk.thip.notification.application.service.template.NotificationTemplate;
import konkuk.thip.notification.domain.value.NotificationCategory;

public enum RoomVoteStartedTemplate implements NotificationTemplate<RoomVoteStartedTemplate.Args> {
    INSTANCE;

    @Override
    public String title(Args args) {
        return NotificationCategory.ROOM.prefixedTitle(args.roomTitle);
    }

    @Override
    public String content(Args args) {
        return "새로운 투표가 시작되었어요!";
    }

    @Override
    public NotificationCategory notificationCategory(Args args) {
        return NotificationCategory.ROOM;
    }

    public record Args(String roomTitle) {}
}
