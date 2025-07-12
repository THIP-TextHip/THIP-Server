package konkuk.thip.user.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import konkuk.thip.user.application.port.in.dto.UserSignupCommand;

public record UserSignupRequest(
        @NotBlank(message = "aliasName은 필수입니다.")
        String aliasName,

        @Pattern(regexp = "[가-힣a-zA-Z0-9]+", message = "닉네임은 한글, 영어, 숫자로만 구성되어야 합니다.(공백불가)")
        @Size(max = 10, message = "닉네임은 최대 10자 입니다.")
        String nickname
) {
    public UserSignupCommand toCommand(String oAuth2Id) {
        return UserSignupCommand.builder()
                .aliasName(aliasName)
                .nickname(nickname)
                .oauth2Id(oAuth2Id)
                .build();
    }
}
