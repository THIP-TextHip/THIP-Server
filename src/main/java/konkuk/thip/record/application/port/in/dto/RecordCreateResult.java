package konkuk.thip.record.application.port.in.dto;

public record RecordCreateResult(
        Long recordId,
        Long roomId
) {
    public static RecordCreateResult of(Long recordId, Long roomId) {
        return new RecordCreateResult(recordId, roomId);
    }
}
