package konkuk.thip.attendancecheck.adapter.out.persistence;

import konkuk.thip.attendancecheck.adapter.out.mapper.AttendanceCheckMapper;
import konkuk.thip.attendancecheck.adapter.out.persistence.repository.AttendanceCheckRepository;
import konkuk.thip.attendancecheck.application.port.out.AttendanceCheckCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AttendanceCheckCommandPersistenceAdapter implements AttendanceCheckCommandPort {

    private final AttendanceCheckRepository attendanceCheckRepository;
    private final AttendanceCheckMapper attendanceCheckMapper;

}
