package konkuk.thip.roompost.adapter.in.web.response;

public record RecordUpdateResponse(
        Long roomId
) {
    public static RecordUpdateResponse of(Long roomId) {
        return new RecordUpdateResponse(roomId);
    }
}
