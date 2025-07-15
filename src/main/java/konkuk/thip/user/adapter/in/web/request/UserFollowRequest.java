package konkuk.thip.user.adapter.in.web.request;

import jakarta.validation.constraints.NotNull;
import konkuk.thip.user.application.port.in.dto.UserFollowCommand;

public record UserFollowRequest(
        @NotNull(message = "type은 필수 파라미터입니다.")
        Boolean type // true -> 팔로우, false -> 언팔로우
) {
        public static UserFollowCommand toCommand(Long userId, Long targetUserId, Boolean type) {
                return new UserFollowCommand(userId, targetUserId, type);
        }
}
