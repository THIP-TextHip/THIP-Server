package konkuk.thip.room.adapter.out.persistence.repository;

import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomParticipantJpaRepository extends JpaRepository<RoomParticipantJpaEntity, Long>{

    Optional<RoomParticipantJpaEntity> findByUserJpaEntity_UserIdAndRoomJpaEntity_RoomId(Long userId, Long roomId);
    List<RoomParticipantJpaEntity> findAllByRoomJpaEntity_RoomId(Long roomId);
}
