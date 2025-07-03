package konkuk.thip.vote.application.service;

import konkuk.thip.vote.application.port.in.VoteCreateUseCase;
import konkuk.thip.vote.application.port.in.dto.VoteCreateCommand;
import konkuk.thip.vote.application.port.out.VoteCommandPort;
import konkuk.thip.vote.domain.Vote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteService implements VoteCreateUseCase {

    private final VoteCommandPort voteCommandPort;

    @Override
    public Long createVote(VoteCreateCommand command) {
        Vote vote = Vote.withoutId(
                command.content(),
                command.userId(),
                command.page(),
                command.isOverview(),
                command.roomId()
        );

        return voteCommandPort.save(vote);
    }
}
