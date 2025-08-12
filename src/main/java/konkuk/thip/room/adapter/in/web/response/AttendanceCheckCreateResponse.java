package konkuk.thip.room.adapter.in.web.response;

public record AttendanceCheckCreateResponse(
        Long attendanceCheckId
) {
    public static AttendanceCheckCreateResponse of(Long attendanceCheckId) {
        return new AttendanceCheckCreateResponse(attendanceCheckId);
    }
}
