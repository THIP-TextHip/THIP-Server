package konkuk.thip.room.adapter.out.persistence.repository.roomparticipant;

import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
            "AND rp.roomJpaEntity.roomId = :roomId")
    Optional<RoomParticipantJpaEntity> findByUserIdAndRoomId(@Param("userId") Long userId, @Param("roomId") Long roomId);

    @Query("SELECT rp FROM RoomParticipantJpaEntity rp " +
            "WHERE rp.roomJpaEntity.roomId = :roomId")
    List<RoomParticipantJpaEntity> findAllByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END " +
            "FROM RoomParticipantJpaEntity rp " +
            "WHERE rp.userJpaEntity.userId = :userId " +
            "AND rp.roomJpaEntity.roomId = :roomId")
    boolean existsByUserIdAndRoomId(@Param("userId") Long userId, @Param("roomId") Long roomId);

    @Query("SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END " +
            "FROM RoomParticipantJpaEntity rp " +
            "JOIN RoomJpaEntity r ON rp.roomJpaEntity.roomId = r.roomId " +
            "WHERE rp.userJpaEntity.userId = :userId " +
            "AND rp.roomParticipantRole = 'HOST' " +
            "AND (r.roomStatus = 'IN_PROGRESS' OR r.roomStatus = 'RECRUITING')")
    boolean existsHostUserInActiveRoom(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RoomParticipantJpaEntity rp SET rp.status = 'INACTIVE' WHERE rp.userJpaEntity.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query("SELECT rp.roomJpaEntity.roomId FROM RoomParticipantJpaEntity rp WHERE rp.userJpaEntity.userId = :userId")
    List<Long> findRoomIdsByUserId(@Param("userId") Long userId);
}
