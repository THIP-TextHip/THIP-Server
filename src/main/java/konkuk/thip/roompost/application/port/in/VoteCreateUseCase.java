package konkuk.thip.roompost.application.port.in;

import konkuk.thip.roompost.application.port.in.dto.vote.VoteCreateCommand;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteCreateResult;

public interface VoteCreateUseCase {

    VoteCreateResult createVote(VoteCreateCommand command);
}
