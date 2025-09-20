package konkuk.thip.notification.adapter.in.web.response;

import java.util.List;

public record NotificationShowResponse(
        List<NotificationOfUser> notifications,
        String nextCursor,
        boolean isLast
) {
    public record NotificationOfUser(
            Long notificationId,
            String title,
            String content,
            boolean isChecked,
            String notificationType,
            String postDate
    ) {}
}
