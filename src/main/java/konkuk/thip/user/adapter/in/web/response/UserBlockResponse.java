package konkuk.thip.user.adapter.in.web.response;

public record UserBlockResponse(
        boolean isBlocked
) {
    public static UserBlockResponse of(boolean isBlocked) {
        return new UserBlockResponse(isBlocked);
    }
}
