package konkuk.thip.user.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import konkuk.thip.user.application.port.in.dto.UserUpdateCommand;

@Schema(description = "사용자 정보 수정 요청 DTO")
public record UserUpdateRequest(

        @Schema(description = "사용자 수정 칭호", example = "문학가")
        @NotBlank(message = "칭호는 필수입니다.")
        String aliasName,

        @Schema(description = "사용자 수정 닉네임", example = "thip")
        String nickname
) {
    public UserUpdateCommand toCommand(Long userId) {
        return new UserUpdateCommand(aliasName, nickname, userId);
    }
}
