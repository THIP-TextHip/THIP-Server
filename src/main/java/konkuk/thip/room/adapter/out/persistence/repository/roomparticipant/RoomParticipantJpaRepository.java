package konkuk.thip.room.adapter.out.persistence.repository.roomparticipant;

import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomParticipantJpaRepository extends JpaRepository<RoomParticipantJpaEntity, Long>, RoomParticipantQueryRepository{

    /**
     * 소프트 딜리트 적용 대상 entity 단건 조회 메서드
     */
    Optional<RoomParticipantJpaEntity> findByRoomParticipantId(Long roomParticipantId);

    @Query("SELECT rp FROM RoomParticipantJpaEntity rp " +
            "WHERE rp.userJpaEntity.userId = :userId " +
            "AND rp.roomJpaEntity.roomId = :roomId " +
            "AND rp.status = 'ACTIVE'")
    Optional<RoomParticipantJpaEntity> findByUserIdAndRoomId(@Param("userId") Long userId, @Param("roomId") Long roomId);

    @Query("SELECT rp FROM RoomParticipantJpaEntity rp " +
            "WHERE rp.roomJpaEntity.roomId = :roomId " +
            "AND rp.status = 'ACTIVE'")
    List<RoomParticipantJpaEntity> findAllByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END " +
            "FROM RoomParticipantJpaEntity rp " +
            "WHERE rp.userJpaEntity.userId = :userId " +
            "AND rp.roomJpaEntity.roomId = :roomId " +
            "AND rp.status = 'ACTIVE'")
    boolean existsByUserIdAndRoomId(@Param("userId") Long userId, @Param("roomId") Long roomId);

}
