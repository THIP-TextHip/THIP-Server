package konkuk.thip.room.adapter.out.persistence;

import konkuk.thip.room.adapter.out.mapper.AttendanceCheckMapper;
import konkuk.thip.room.adapter.out.persistence.repository.attendanceCheck.AttendanceCheckJpaRepository;
import konkuk.thip.room.application.port.out.AttendanceCheckQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AttendanceCheckQueryPersistenceAdapter implements AttendanceCheckQueryPort {

    private final AttendanceCheckJpaRepository jpaRepository;
    private final AttendanceCheckMapper attendanceCheckMapper;

}
