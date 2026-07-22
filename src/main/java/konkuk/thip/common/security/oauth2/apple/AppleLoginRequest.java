package konkuk.thip.common.security.oauth2.apple;

import jakarta.validation.constraints.NotBlank;

public record AppleLoginRequest(
        @NotBlank String identityToken,
        String authorizationCode
) {
}
