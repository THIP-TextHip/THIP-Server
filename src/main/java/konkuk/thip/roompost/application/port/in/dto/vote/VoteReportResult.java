package konkuk.thip.roompost.application.port.in.dto.vote;

public record VoteReportResult(
        Long voteId,
        int reportCount
) {
    public static VoteReportResult of(Long voteId, int reportCount) {
        return new VoteReportResult(voteId, reportCount);
    }
}
