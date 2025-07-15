package konkuk.thip.attendancecheck.adapter.out.persistence.repository;

import konkuk.thip.attendancecheck.adapter.out.jpa.AttendanceCheckJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceCheckJpaRepository extends JpaRepository<AttendanceCheckJpaEntity, Long> {
}
