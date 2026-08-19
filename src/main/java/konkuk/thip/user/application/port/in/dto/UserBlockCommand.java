package konkuk.thip.user.application.port.in.dto;

public record UserBlockCommand(Long userId, Long targetUserId, Boolean type) {
}
