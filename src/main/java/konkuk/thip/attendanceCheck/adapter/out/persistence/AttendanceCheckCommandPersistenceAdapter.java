package konkuk.thip.attendanceCheck.adapter.out.persistence;

import konkuk.thip.attendanceCheck.adapter.out.mapper.AttendanceCheckMapper;
import konkuk.thip.attendanceCheck.adapter.out.persistence.repository.AttendanceCheckRepository;
import konkuk.thip.attendanceCheck.application.port.out.AttendanceCheckCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AttendanceCheckCommandPersistenceAdapter implements AttendanceCheckCommandPort {

    private final AttendanceCheckRepository attendanceCheckRepository;
    private final AttendanceCheckMapper attendanceCheckMapper;

}
