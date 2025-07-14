package konkuk.thip.user.adapter.in.web.request;

import jakarta.validation.constraints.NotNull;

public record UserFollowRequest(
        @NotNull(message = "type은 필수 파라미터입니다.")
        Boolean type // true -> 팔로우, false -> 언팔로우
) {

}
