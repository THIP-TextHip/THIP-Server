package konkuk.thip.roompost.adapter.in.web.response;

import konkuk.thip.roompost.application.port.in.dto.vote.VoteReportResult;

public record VoteReportResponse(
        Long voteId,
        int reportCount
) {
    public static VoteReportResponse of(VoteReportResult voteReportResult) {
        return new VoteReportResponse(voteReportResult.voteId(), voteReportResult.reportCount());
    }
}
