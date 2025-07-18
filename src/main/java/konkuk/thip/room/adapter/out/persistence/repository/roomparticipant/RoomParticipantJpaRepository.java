package konkuk.thip.room.adapter.out.persistence.repository.roomparticipant;

import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomParticipantJpaRepository extends JpaRepository<RoomParticipantJpaEntity, Long>, RoomParticipantQueryRepository{

    @Query(value = "SELECT * FROM room_participants WHERE user_id = :userId AND room_id = :roomId", nativeQuery = true)
    Optional<RoomParticipantJpaEntity> findByUserIdAndRoomId(@Param("userId") Long userId, @Param("roomId") Long roomId);

    List<RoomParticipantJpaEntity> findAllByRoomJpaEntity_RoomId(Long roomId);

    @Query(
            value = "SELECT EXISTS (SELECT 1 FROM room_participants rp WHERE rp.user_id = :userId AND rp.room_id = :roomId AND rp.status = 'ACTIVE')",
            nativeQuery = true
    )
    boolean existByUserIdAndRoomId(@Param("userId") Long userId, @Param("roomId") Long roomId);

}
