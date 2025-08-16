package konkuk.thip.roompost.application.port.in;

import konkuk.thip.roompost.application.port.in.dto.vote.VoteCommand;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteResult;

public interface VoteUseCase {
    VoteResult vote(VoteCommand command);
}
