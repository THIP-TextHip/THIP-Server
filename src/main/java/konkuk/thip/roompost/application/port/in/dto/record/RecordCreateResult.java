package konkuk.thip.roompost.application.port.in.dto.record;

public record RecordCreateResult(
        Long recordId,
        Long roomId
) {
    public static RecordCreateResult of(Long recordId, Long roomId) {
        return new RecordCreateResult(recordId, roomId);
    }
}
