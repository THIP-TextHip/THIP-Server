package konkuk.thip.user.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "사용자 닉네임 중복 확인 요청 DTO")
public record UserVerifyNicknameRequest(
        @Schema(description = "사용자 닉네임", example = "홍길동_123")
        @Pattern(regexp = "[가-힣a-zA-Z0-9]+", message = "닉네임은 한글, 영어, 숫자로만 구성되어야 합니다.(공백불가)")
        @Size(max = 10, message = "닉네임은 최대 10자 입니다.")
        String nickname
) {
}
