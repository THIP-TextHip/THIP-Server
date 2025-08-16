package konkuk.thip.roompost.application.port.in.dto.record;

public record RecordPinQuery(
        Long roomId,

        Long recordId,

        Long userId
) {
}
