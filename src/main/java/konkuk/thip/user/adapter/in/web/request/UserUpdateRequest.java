package konkuk.thip.user.adapter.in.web.request;

import lombok.Getter;

@Getter
public class UserUpdateRequest {

    private Long id;

    private String name;

    private String password;
}
