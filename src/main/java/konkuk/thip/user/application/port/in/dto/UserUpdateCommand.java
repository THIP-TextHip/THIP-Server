package konkuk.thip.user.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdateCommand {

    private Long id;

    private String name;

    private String password;
}
