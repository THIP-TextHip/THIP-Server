package konkuk.thip.attendancecheck.adapter.out.persistence;

import konkuk.thip.attendancecheck.adapter.out.mapper.AttendanceCheckMapper;
import konkuk.thip.attendancecheck.adapter.out.persistence.repository.AttendanceCheckRepository;
import konkuk.thip.attendancecheck.application.port.out.AttendnaceCheckQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AttendanceCheckQueryPersistenceAdapter implements AttendnaceCheckQueryPort {

    private final AttendanceCheckRepository jpaRepository;
    private final AttendanceCheckMapper attendanceCheckMapper;

}
