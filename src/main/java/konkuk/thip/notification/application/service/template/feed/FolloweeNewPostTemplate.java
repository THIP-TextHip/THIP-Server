package konkuk.thip.notification.application.service.template.feed;

import konkuk.thip.notification.application.service.template.NotificationTemplate;
import konkuk.thip.notification.domain.value.NotificationCategory;

public enum FolloweeNewPostTemplate implements NotificationTemplate<FolloweeNewPostTemplate.Args> {
    INSTANCE;

    @Override
    public String title(Args args) {
        return NotificationCategory.FEED.prefixedTitle("새 글 알림");
    }

    @Override
    public String content(Args args) {
        return "@" + args.actorUsername() + " 님이 새로운 글을 작성했어요!";
    }

    @Override
    public NotificationCategory notificationCategory(Args args) {
        return NotificationCategory.FEED;
    }

    public record Args(String actorUsername) {}
}
