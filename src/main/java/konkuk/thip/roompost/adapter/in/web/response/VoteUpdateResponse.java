package konkuk.thip.roompost.adapter.in.web.response;

public record VoteUpdateResponse(
        Long roomId
) {
    public static VoteUpdateResponse of(Long roomId) {
        return new VoteUpdateResponse(roomId);
    }
}
