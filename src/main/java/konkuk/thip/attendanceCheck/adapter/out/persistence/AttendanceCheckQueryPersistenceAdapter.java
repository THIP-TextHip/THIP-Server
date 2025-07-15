package konkuk.thip.attendanceCheck.adapter.out.persistence;

import konkuk.thip.attendanceCheck.adapter.out.mapper.AttendanceCheckMapper;
import konkuk.thip.attendanceCheck.adapter.out.persistence.repository.AttendanceCheckRepository;
import konkuk.thip.attendanceCheck.application.port.out.AttendnaceCheckQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AttendanceCheckQueryPersistenceAdapter implements AttendnaceCheckQueryPort {

    private final AttendanceCheckRepository jpaRepository;
    private final AttendanceCheckMapper attendanceCheckMapper;

}
