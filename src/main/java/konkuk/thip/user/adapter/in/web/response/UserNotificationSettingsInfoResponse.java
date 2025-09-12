package konkuk.thip.user.adapter.in.web.response;

public record UserNotificationSettingsInfoResponse(
        boolean isEnabled
) {
    public static UserNotificationSettingsInfoResponse of(boolean isEnabled) {
        return new UserNotificationSettingsInfoResponse(isEnabled);
    }
}
