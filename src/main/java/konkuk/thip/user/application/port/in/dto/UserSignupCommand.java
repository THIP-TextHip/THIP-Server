package konkuk.thip.user.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserSignupCommand {

    private String name;

    private String email;

    private String password;
}
