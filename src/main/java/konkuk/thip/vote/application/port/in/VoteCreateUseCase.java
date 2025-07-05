package konkuk.thip.vote.application.port.in;

import konkuk.thip.vote.application.port.in.dto.VoteCreateCommand;

public interface VoteCreateUseCase {

    Long createVote(VoteCreateCommand command);
}
