package konkuk.thip.user.application.port.in.dto;

public record UserUpdateCommand(
        String aliasName,
        String nickname,
        Long userId
) {
}
