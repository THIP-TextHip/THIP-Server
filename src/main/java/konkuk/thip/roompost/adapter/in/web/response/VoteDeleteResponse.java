package konkuk.thip.roompost.adapter.in.web.response;

public record VoteDeleteResponse(Long roomId) {
    public static VoteDeleteResponse of(Long roomId) {
        return new VoteDeleteResponse(roomId);
    }
}
