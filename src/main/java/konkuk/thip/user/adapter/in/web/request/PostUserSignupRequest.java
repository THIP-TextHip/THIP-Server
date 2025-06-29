package konkuk.thip.user.adapter.in.web.request;

import jakarta.validation.constraints.*;
import konkuk.thip.user.application.port.in.dto.UserSignupCommand;

public record PostUserSignupRequest(
        @NotNull(message = "aliasId는 필수입니다.")
        Long aliasId,

        @Pattern(regexp = "[가-힣a-zA-Z0-9]+", message = "닉네임은 한글, 영어, 숫자로만 구성되어야 합니다.(공백불가)")
        @Size(max = 10, message = "닉네임은 최대 10자 입니다.")
        String nickname,

        @NotBlank(message = "이메일은 공백일 수 없습니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email
) {
    public UserSignupCommand toCommand() {
        return UserSignupCommand.builder()
                .aliasId(aliasId)
                .nickname(nickname)
                .email(email)
                .build();
    }
}
