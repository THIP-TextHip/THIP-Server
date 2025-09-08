package konkuk.thip.roompost.adapter.out.persistence.repository.attendancecheck;

import konkuk.thip.roompost.adapter.out.jpa.AttendanceCheckJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AttendanceCheckJpaRepository extends JpaRepository<AttendanceCheckJpaEntity, Long>, AttendanceCheckQueryRepository {

    /**
     * 소프트 딜리트 적용 대상 entity 단건 조회 메서드
     */
    Optional<AttendanceCheckJpaEntity> findByAttendanceCheckId(Long attendanceCheckId);

    // TODO : count 값을 매번 쿼리를 통해 계산하는게 아니라 DB에 저장 or redis 캐시에 저장하는 방법도 좋을 듯
    @Query("SELECT COUNT(a) FROM AttendanceCheckJpaEntity a " +
            "WHERE a.userJpaEntity.userId = :userId " +
            "AND a.roomJpaEntity.roomId = :roomId " +
            "AND a.createdAt >= :startOfDay " +
            "AND a.createdAt < :endOfDay")
    int countByUserIdAndRoomIdAndCreatedAtBetween(@Param("userId") Long userId, @Param("roomId") Long roomId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AttendanceCheckJpaEntity a SET a.status = 'INACTIVE' WHERE a.userJpaEntity.userId = :userId")
    void softDeleteAllByUserId(@Param("userId") Long userId);
}
