package konkuk.thip.notification.adapter.in.web.response;

public record NotificationShowEnableStateResponse(
        boolean isEnabled
) {
    public static NotificationShowEnableStateResponse of(boolean isEnabled) {
        return new NotificationShowEnableStateResponse(isEnabled);
    }
}
