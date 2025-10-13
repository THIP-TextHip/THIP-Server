package konkuk.thip.notification.adapter.in.web.response;

public record NotificationUncheckedExistsResponse(
        boolean exists
) {
    public static NotificationUncheckedExistsResponse of(boolean exists) {
        return new NotificationUncheckedExistsResponse(exists);
    }
}
