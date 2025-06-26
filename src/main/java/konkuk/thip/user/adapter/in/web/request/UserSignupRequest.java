package konkuk.thip.user.adapter.in.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import konkuk.thip.user.application.port.in.dto.UserSignupCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupRequest {

    @NotNull(message = "aliasId는 필수입니다.")
    private Long aliasId;

    @NotBlank(message = "닉네임은 공백일 수 없습니다.")
    @Length(max = 10, message = "닉네임은 최대 10자 입니다.")
    private String nickname;

    @NotBlank(message = "이메일은 공백일 수 없습니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    public UserSignupCommand toCommand() {
        return UserSignupCommand.builder()
                .aliasId(aliasId)
                .nickname(nickname)
                .email(email)
                .build();
    }
}
