package konkuk.thip.user.adapter.out.persistence.repository;

import konkuk.thip.user.adapter.out.jpa.UserRoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoomJpaRepository extends JpaRepository<UserRoomJpaEntity, Long>{

    Optional<UserRoomJpaEntity> findByUserJpaEntity_UserIdAndRoomJpaEntity_RoomId(Long userId, Long roomId);
    List<UserRoomJpaEntity> findAllByRoomJpaEntity_RoomId(Long roomId);
}
