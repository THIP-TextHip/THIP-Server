package konkuk.thip.vote.adapter.out.persistence;

import konkuk.thip.vote.adapter.out.jpa.UserVoteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserVoteJpaRepository extends JpaRepository<UserVoteJpaEntity, Long> {
    boolean existsByUserJpaEntity_UserIdAndVoteItemJpaEntity_VoteItemId(Long userId, Long voteItemId);
}
