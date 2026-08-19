package konkuk.thip.user.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import konkuk.thip.user.application.port.in.dto.UserBlockCommand;

@Schema(description = "사용자 차단 상태 변경 요청 DTO")
public record UserBlockRequest(
        @Schema(description = "true -> 차단, false -> 차단 해제", example = "true")
        @NotNull(message = "type은 필수 파라미터입니다.")
        Boolean type
) {
        public UserBlockCommand toCommand(Long userId, Long targetUserId) {
                return new UserBlockCommand(userId, targetUserId, type);
        }
}
