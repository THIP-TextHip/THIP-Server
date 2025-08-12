package konkuk.thip.attendancecheck.adapter.out.persistence;

import konkuk.thip.attendancecheck.adapter.out.mapper.AttendanceCheckMapper;
import konkuk.thip.attendancecheck.adapter.out.persistence.repository.AttendanceCheckJpaRepository;
import konkuk.thip.attendancecheck.application.port.out.AttendanceCheckCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AttendanceCheckCommandPersistenceAdapter implements AttendanceCheckCommandPort {

    private final AttendanceCheckJpaRepository attendanceCheckJpaRepository;
    private final AttendanceCheckMapper attendanceCheckMapper;

}
