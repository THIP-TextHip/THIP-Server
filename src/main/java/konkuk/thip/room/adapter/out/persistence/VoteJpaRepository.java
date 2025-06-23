package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.room.adapter.out.jpa.VoteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteJpaRepository extends JpaRepository<VoteJpaEntity, Long> {
}
