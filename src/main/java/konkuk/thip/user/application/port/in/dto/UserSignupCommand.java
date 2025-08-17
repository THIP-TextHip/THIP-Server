package konkuk.thip.user.application.port.in.dto;

import lombok.Builder;

@Builder
public record UserSignupCommand(
        String aliasName,
        String nickname,
        boolean isTokenRequired,
        String oauth2Id
) {}
