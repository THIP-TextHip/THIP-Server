package konkuk.thip.user.adapter.in.web.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserReadResponse {

    private Long id;

    private String name;

    private String email;
}
