package konkuk.thip.room.adapter.out.persistence.repository.attendancecheck;

import konkuk.thip.room.adapter.out.jpa.AttendanceCheckJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AttendanceCheckJpaRepository extends JpaRepository<AttendanceCheckJpaEntity, Long> {

    // TODO : count 값을 매번 쿼리를 통해 계산하는게 아니라 DB에 저장 or redis 캐시에 저장하는 방법도 좋을 듯
    @Query("SELECT COUNT(a) FROM AttendanceCheckJpaEntity a " +
            "WHERE a.userJpaEntity.userId = :userId " +
            "AND a.createdAt >= :startOfDay " +
            "AND a.createdAt < :endOfDay")
    int countByUserIdAndCreatedAtBetween(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
}
