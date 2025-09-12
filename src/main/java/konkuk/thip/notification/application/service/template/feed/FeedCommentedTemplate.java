package konkuk.thip.notification.application.service.template.feed;

import konkuk.thip.notification.application.service.template.NotificationTemplate;
import konkuk.thip.notification.domain.value.NotificationCategory;

public enum FeedCommentedTemplate implements NotificationTemplate<FeedCommentedTemplate.Args> {
    INSTANCE;

    @Override
    public String title(Args args) {
        return NotificationCategory.FEED.prefixedTitle("새로운 댓글이 달렸어요");
    }

    @Override
    public String content(Args args) {
        return "@" + args.actorUsername() + " 님이 내 글에 댓글을 달았어요!";
    }

    public record Args(String actorUsername) {}
}
