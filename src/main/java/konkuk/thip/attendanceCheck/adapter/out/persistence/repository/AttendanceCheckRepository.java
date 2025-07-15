package konkuk.thip.attendanceCheck.adapter.out.persistence.repository;

import konkuk.thip.attendanceCheck.adapter.out.jpa.AttendanceCheckJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceCheckRepository extends JpaRepository<AttendanceCheckJpaEntity, Long> {
}
