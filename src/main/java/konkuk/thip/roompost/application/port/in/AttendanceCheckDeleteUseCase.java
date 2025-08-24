package konkuk.thip.roompost.application.port.in;

public interface AttendanceCheckDeleteUseCase {

    Long delete(Long creatorId, Long roomId, Long attendanceCheckId);

}
