package konkuk.thip.record.application.port.in.dto;

public record RecordDeleteCommand(
        Long roomId,

        Long recordId,

        Long userId
) {
}
