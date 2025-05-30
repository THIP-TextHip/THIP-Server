package konkuk.thip.user.adapter.in.web.request;

import lombok.Getter;

@Getter
public class UserSignupRequest {

    private String name;

    private String email;

    private String password;
}
