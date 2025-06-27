package konkuk.thip.user.application.port.in.dto;

import lombok.Builder;

@Builder
public record UserSignupCommand(
        Long aliasId,
        String nickname,
        String email,
        String oauth2Id
) {}
