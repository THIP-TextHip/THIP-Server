package konkuk.thip.room.application.port.in.dto;

public record RoomReportResult(
        Long roomId,
        int reportCount
) {
    public static RoomReportResult of(Long roomId, int reportCount) {
        return new RoomReportResult(roomId, reportCount);
    }
}
