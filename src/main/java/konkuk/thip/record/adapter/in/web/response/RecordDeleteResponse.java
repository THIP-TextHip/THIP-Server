package konkuk.thip.record.adapter.in.web.response;

public record RecordDeleteResponse(Long roomId) {
    public static RecordDeleteResponse of(Long roomId) {
        return new RecordDeleteResponse(roomId);
    }
}
