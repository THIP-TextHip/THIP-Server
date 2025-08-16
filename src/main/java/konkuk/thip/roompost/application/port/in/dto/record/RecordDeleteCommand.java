package konkuk.thip.roompost.application.port.in.dto.record;

public record RecordDeleteCommand(
        Long roomId,

        Long recordId,

        Long userId
) {
}
