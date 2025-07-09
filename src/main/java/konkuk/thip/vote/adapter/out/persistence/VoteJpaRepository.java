package konkuk.thip.vote.adapter.out.persistence;

import konkuk.thip.vote.adapter.out.jpa.VoteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteJpaRepository extends JpaRepository<VoteJpaEntity, Long>, VoteQueryRepository {

}
