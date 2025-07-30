package konkuk.thip.user.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import konkuk.thip.user.application.port.in.dto.UserFollowCommand;

@Schema(description = "사용자 팔로우 상태 변경 요청 DTO")
public record UserFollowRequest(
        @Schema(description = "true -> 팔로우, false -> 언팔로우", example = "true")
        @NotNull(message = "type은 필수 파라미터입니다.")
        Boolean type
) {
        public static UserFollowCommand toCommand(Long userId, Long targetUserId, Boolean type) {
                return new UserFollowCommand(userId, targetUserId, type);
        }
}
