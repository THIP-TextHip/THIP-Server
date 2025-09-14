package konkuk.thip.notification.application.service.template.room;

import konkuk.thip.notification.application.service.template.NotificationTemplate;
import konkuk.thip.notification.domain.value.NotificationCategory;

public enum RoomRecruitClosedEarlyTemplate implements NotificationTemplate<RoomRecruitClosedEarlyTemplate.Args> {
    INSTANCE;

    @Override
    public String title(Args args) {
        return NotificationCategory.ROOM.prefixedTitle(args.roomTitle);
    }

    @Override
    public String content(Args args) {
        return "모임방 활동이 시작되었어요. 모임방에서 독서 기록을 시작해보세요!";
    }

    public record Args(String roomTitle) {}
}
