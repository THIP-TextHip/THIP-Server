package konkuk.thip.roompost.application.port.in;

import konkuk.thip.roompost.application.port.in.dto.vote.VoteDeleteCommand;

public interface VoteDeleteUseCase {
    Long deleteVote(VoteDeleteCommand command);
}
