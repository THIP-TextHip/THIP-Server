package konkuk.thip.notification.application.service.template.feed;

import konkuk.thip.notification.application.service.template.NotificationTemplate;
import konkuk.thip.notification.domain.value.NotificationCategory;

public enum FeedLikedTemplate implements NotificationTemplate<FeedLikedTemplate.Args> {
    INSTANCE;

    @Override
    public String title(Args args) {
        return NotificationCategory.FEED.prefixedTitle("내 글을 좋아합니다");
    }

    @Override
    public String content(Args args) {
        return "@" + args.actorUsername() + " 님이 내 글에 좋아요를 눌렀어요!";
    }

    @Override
    public NotificationCategory notificationCategory(Args args) {
        return NotificationCategory.FEED;
    }

    public record Args(String actorUsername) {}
}
