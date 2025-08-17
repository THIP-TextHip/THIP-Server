package konkuk.thip.user.application.port.in.dto;

import lombok.Builder;

@Builder
public record UserSignupCommand(
        String aliasName,
        String nickname,
        String oauth2Id
) {}
