package konkuk.thip.record.adapter.in.web.response;

public record RecordCreateResponse(
        Long recordId
) {
    public static RecordCreateResponse of(Long recordId) {
        return new RecordCreateResponse(recordId);
    }
}
