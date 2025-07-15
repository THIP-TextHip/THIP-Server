package konkuk.thip.user.adapter.in.web.response;

public record UserFollowResponse(
        boolean isFollowing
) {
    public static UserFollowResponse of(boolean isFollowing) {
        return new UserFollowResponse(isFollowing);
    }
}
