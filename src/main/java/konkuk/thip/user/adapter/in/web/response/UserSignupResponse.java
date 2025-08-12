package konkuk.thip.user.adapter.in.web.response;

import konkuk.thip.user.application.port.in.dto.UserSignupResult;

public record UserSignupResponse(
        Long userId,
        String accessToken
        ) {
    public static UserSignupResponse of(UserSignupResult userSignupResult) {
        return new UserSignupResponse(userSignupResult.userId(), userSignupResult.accessToken());
    }
}