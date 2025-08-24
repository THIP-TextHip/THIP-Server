package konkuk.thip.roompost.adapter.in.web.response;

public record AttendanceCheckDeleteResponse(
        Long roomId
) {
    public static AttendanceCheckDeleteResponse of(Long roomId) {
        return new AttendanceCheckDeleteResponse(roomId);
    }
}
