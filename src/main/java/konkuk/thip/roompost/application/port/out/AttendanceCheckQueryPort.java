package konkuk.thip.roompost.application.port.out;

import konkuk.thip.common.util.Cursor;
import konkuk.thip.common.util.CursorBasedList;
import konkuk.thip.roompost.application.port.out.dto.AttendanceCheckQueryDto;

public interface AttendanceCheckQueryPort {

    int countAttendanceChecksOnTodayByUser(Long userId, Long roomId);

    CursorBasedList<AttendanceCheckQueryDto> findAttendanceChecksByCreatedAtDesc(Long roomId, Cursor cursor);
}
