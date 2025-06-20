package konkuk.thip.domain.room.adapter.out.persistence;

import konkuk.thip.domain.room.adapter.out.jpa.VoteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteJpaRepository extends JpaRepository<VoteJpaEntity, Long> {
}
