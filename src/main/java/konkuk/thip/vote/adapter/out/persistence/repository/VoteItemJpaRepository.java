package konkuk.thip.vote.adapter.out.persistence.repository;

import konkuk.thip.vote.adapter.out.jpa.VoteItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteItemJpaRepository extends JpaRepository<VoteItemJpaEntity, Long> {

    List<VoteItemJpaEntity> findAllByVoteJpaEntity_PostId(Long voteId);
}
