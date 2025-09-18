package konkuk.thip.notification.application.service.template.room;

import konkuk.thip.notification.application.service.template.NotificationTemplate;
import konkuk.thip.notification.domain.value.NotificationCategory;

public enum RoomRecordCreatedTemplate implements NotificationTemplate<RoomRecordCreatedTemplate.Args> {
    INSTANCE;

    @Override
    public String title(Args args) {
        return NotificationCategory.ROOM.prefixedTitle(args.roomTitle);
    }

    @Override
    public String content(Args args) {
        return "@" + args.actorUsername() + " 님이 새로운 독서 기록을 작성했어요!";
    }

    @Override
    public NotificationCategory notificationCategory(Args args) {
        return NotificationCategory.ROOM;
    }

    public record Args(String roomTitle, String actorUsername) {}
}
