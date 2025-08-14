package konkuk.thip.vote.application.port.in;

import konkuk.thip.vote.application.port.in.dto.VoteDeleteCommand;

public interface VoteDeleteUseCase {
    Long deleteVote(VoteDeleteCommand command);
}
