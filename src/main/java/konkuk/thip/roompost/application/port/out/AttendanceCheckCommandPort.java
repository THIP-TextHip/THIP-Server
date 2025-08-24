package konkuk.thip.roompost.application.port.out;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.roompost.domain.AttendanceCheck;

import java.util.Optional;

import static konkuk.thip.common.exception.code.ErrorCode.ATTENDANCE_CHECK_NOT_FOUND;

public interface AttendanceCheckCommandPort {

    Long save(AttendanceCheck attendanceCheck);

    Optional<AttendanceCheck> findById(Long id);

    default AttendanceCheck getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ATTENDANCE_CHECK_NOT_FOUND));
    }

    void delete(AttendanceCheck attendanceCheck);
}
