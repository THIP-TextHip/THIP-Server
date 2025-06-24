package konkuk.thip.vote.adapter.out.persistence;

import konkuk.thip.vote.adapter.out.mapper.VoteMapper;
import konkuk.thip.vote.application.port.out.VoteCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VoteCommandPersistenceAdapter implements VoteCommandPort {

    private final VoteJpaRepository voteJpaRepository;
    private final VoteMapper voteMapper;

}
