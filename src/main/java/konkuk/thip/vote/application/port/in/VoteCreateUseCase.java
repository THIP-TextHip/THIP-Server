package konkuk.thip.vote.application.port.in;

import konkuk.thip.vote.application.port.in.dto.VoteCreateCommand;
import konkuk.thip.vote.application.port.in.dto.VoteCreateResult;

public interface VoteCreateUseCase {

    VoteCreateResult createVote(VoteCreateCommand command);
}
