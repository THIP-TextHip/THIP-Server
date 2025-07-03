package konkuk.thip.vote.application.port.out;

import konkuk.thip.vote.domain.Vote;

public interface VoteCommandPort {

    Long save(Vote vote);
}
