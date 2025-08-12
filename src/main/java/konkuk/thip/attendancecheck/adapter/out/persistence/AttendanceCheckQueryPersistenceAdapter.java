package konkuk.thip.attendancecheck.adapter.out.persistence;

import konkuk.thip.attendancecheck.adapter.out.mapper.AttendanceCheckMapper;
import konkuk.thip.attendancecheck.adapter.out.persistence.repository.AttendanceCheckJpaRepository;
import konkuk.thip.attendancecheck.application.port.out.AttendanceCheckQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AttendanceCheckQueryPersistenceAdapter implements AttendanceCheckQueryPort {

    private final AttendanceCheckJpaRepository jpaRepository;
    private final AttendanceCheckMapper attendanceCheckMapper;

}
