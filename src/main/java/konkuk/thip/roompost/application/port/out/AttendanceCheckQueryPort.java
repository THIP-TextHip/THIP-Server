package konkuk.thip.roompost.application.port.out;

public interface AttendanceCheckQueryPort {

    int countAttendanceChecksOnTodayByUser(Long userId, Long roomId);

}
