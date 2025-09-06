package konkuk.thip.notification.adapter.in.web.response;

public record FcmTokenEnableStateChangeResponse(
        boolean isEnabled
) {
    public static FcmTokenEnableStateChangeResponse of(boolean isEnabled) {
        return new FcmTokenEnableStateChangeResponse(isEnabled);
    }
}
