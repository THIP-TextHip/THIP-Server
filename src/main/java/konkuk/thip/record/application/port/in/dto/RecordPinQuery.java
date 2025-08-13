package konkuk.thip.record.application.port.in.dto;

public record RecordPinQuery(
        Long roomId,

        Long recordId,

        Long userId
) {
}
