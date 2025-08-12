package konkuk.thip.room.adapter.out.persistence.repository.attendanceCheck;

import konkuk.thip.room.adapter.out.jpa.AttendanceCheckJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceCheckJpaRepository extends JpaRepository<AttendanceCheckJpaEntity, Long> {
}
