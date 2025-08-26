package konkuk.thip.roompost.application.port.in.dto.record;

public record RecordUpdateCommand(
        Long roomId,
        Long postId,
        Long userId,
        String content
) {
}
