package konkuk.thip.vote.application.port.in;

import konkuk.thip.vote.application.service.dto.VoteCommand;
import konkuk.thip.vote.application.service.dto.VoteResult;

public interface VoteUseCase {
    VoteResult vote(VoteCommand command);
}
