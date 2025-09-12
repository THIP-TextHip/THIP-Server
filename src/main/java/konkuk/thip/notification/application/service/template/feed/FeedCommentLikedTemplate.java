package konkuk.thip.notification.application.service.template.feed;

import konkuk.thip.notification.application.service.template.NotificationTemplate;
import konkuk.thip.notification.domain.value.NotificationCategory;

public enum FeedCommentLikedTemplate implements NotificationTemplate<FeedCommentLikedTemplate.Args> {
    INSTANCE;

    @Override
    public String title(Args args) {
        return NotificationCategory.FEED.prefixedTitle("좋아요 알림");
    }

    @Override
    public String content(Args args) {
        return "@" + args.actorUsername() + " 님이 내 댓글에 좋아요를 눌렀어요!";
    }

    public record Args(String actorUsername) {}
}
