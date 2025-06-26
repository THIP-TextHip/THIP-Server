package konkuk.thip.user.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSignupCommand {

    private Long aliasId;

    private String nickname;

    private String email;
}
