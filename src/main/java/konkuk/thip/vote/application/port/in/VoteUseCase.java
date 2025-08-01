package konkuk.thip.vote.application.port.in;

import konkuk.thip.vote.application.port.in.dto.VoteCommand;
import konkuk.thip.vote.application.port.in.dto.VoteResult;

public interface VoteUseCase {
    VoteResult vote(VoteCommand command);
}
