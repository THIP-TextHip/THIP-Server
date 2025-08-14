package konkuk.thip.room.application.port.out;

public interface AttendanceCheckQueryPort {

    int countAttendanceChecksOnTodayByUser(Long userId, Long roomId);

}
