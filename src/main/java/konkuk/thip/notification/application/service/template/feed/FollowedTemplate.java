package konkuk.thip.notification.application.service.template.feed;

import konkuk.thip.notification.application.service.template.NotificationTemplate;
import konkuk.thip.notification.domain.value.NotificationCategory;
import lombok.Getter;

@Getter
public enum FollowedTemplate implements NotificationTemplate<FollowedTemplate.Args> {
    INSTANCE;

    @Override
    public String title(Args args) {
        return NotificationCategory.FEED.prefixedTitle("팔로워 알림");
    }

    @Override
    public String content(Args args) {
        return "@" + args.actorUsername() + " 님이 나를 띱했어요!";
    }

    public record Args(String actorUsername) {}
}
