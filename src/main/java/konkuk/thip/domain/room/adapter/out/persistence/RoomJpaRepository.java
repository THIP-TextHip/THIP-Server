package konkuk.thip.domain.room.adapter.out.persistence;

import konkuk.thip.domain.room.adapter.out.jpa.RoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomJpaRepository extends JpaRepository<RoomJpaEntity, Long> {
}
