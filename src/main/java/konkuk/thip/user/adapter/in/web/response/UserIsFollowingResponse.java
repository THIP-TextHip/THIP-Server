package konkuk.thip.user.adapter.in.web.response;

public record UserIsFollowingResponse(
    boolean isFollowing
) {
    public static UserIsFollowingResponse of(boolean isFollowing) {
        return new UserIsFollowingResponse(isFollowing);
    }
}
