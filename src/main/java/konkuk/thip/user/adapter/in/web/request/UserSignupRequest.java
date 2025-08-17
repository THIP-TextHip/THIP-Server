package konkuk.thip.user.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import konkuk.thip.user.application.port.in.dto.UserSignupCommand;

@Schema(description = "사용자 회원가입 요청 DTO")
public record UserSignupRequest(
        @Schema(description = "사용자 칭호", example = "문학가")
        @NotBlank(message = "aliasName은 필수입니다.")
        String aliasName,

        @Schema(description = "사용자 닉네임", example = "thip")
        @NotNull(message = "닉네임은 필수입니다.")
        @Pattern(regexp = "[가-힣a-z0-9]+", message = "닉네임은 한글, 영어 소문자, 숫자로만 구성되어야 합니다.(공백불가)")
        @Size(max = 10, message = "닉네임은 최대 10자 입니다.")
        String nickname,

        @Schema(description = "토큰 발급 여부 (웹에서는 false / 안드는 true 또는 안줘도 됩니다.) null일 경우 true로 간주", example = "false")
        Boolean isTokenRequired
) {
    public UserSignupCommand toCommand(String oAuth2Id) {
        return UserSignupCommand.builder()
                .aliasName(aliasName)
                .nickname(nickname)
                .isTokenRequired(isTokenRequired == null ? true : isTokenRequired)
                .oauth2Id(oAuth2Id)
                .build();
    }
}
