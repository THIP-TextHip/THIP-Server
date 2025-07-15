package konkuk.thip.user.application.port.in.dto;

public record UserFollowCommand(
        Long userId,
        Long targetUserId,
        Boolean type // true -> 팔로우, false -> 언팔로우
) {
}
