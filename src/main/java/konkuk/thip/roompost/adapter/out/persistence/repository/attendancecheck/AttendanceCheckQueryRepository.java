package konkuk.thip.roompost.adapter.out.persistence.repository.attendancecheck;

import konkuk.thip.roompost.application.port.out.dto.AttendanceCheckQueryDto;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceCheckQueryRepository {

    List<AttendanceCheckQueryDto> findAttendanceChecksByCreatedAtDesc(Long roomId, LocalDateTime lastCreatedAt, int size);
}
