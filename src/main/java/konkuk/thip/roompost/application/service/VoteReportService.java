package konkuk.thip.roompost.application.service;

import konkuk.thip.roompost.application.port.in.VoteReportUseCase;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteReportResult;
import konkuk.thip.roompost.application.port.out.VoteCommandPort;
import konkuk.thip.roompost.domain.Vote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteReportService implements VoteReportUseCase {

    private final VoteCommandPort voteCommandPort;

    @Override
    @Transactional
    public VoteReportResult reportVote(Long voteId) {
        Vote vote = voteCommandPort.getByIdOrThrow(voteId);
        vote.increaseReportCount();
        voteCommandPort.updateVote(vote);

        return VoteReportResult.of(vote.getId(), vote.getReportCount());
    }
}
