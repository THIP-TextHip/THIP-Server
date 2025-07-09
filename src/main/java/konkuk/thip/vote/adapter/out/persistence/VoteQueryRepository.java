package konkuk.thip.vote.adapter.out.persistence;

import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;

import java.util.List;

public interface VoteQueryRepository {

    List<VoteJpaEntity> findVotesByRoom(Long roomId, String type, Integer pageStart, Integer pageEnd, Long userId);
}
