package konkuk.thip.roompost.application.port.in;

import konkuk.thip.roompost.application.port.in.dto.vote.VoteReportResult;

public interface VoteReportUseCase {
    VoteReportResult reportVote(Long voteId);
}
