package konkuk.thip.room.adapter.in.web.response;

import konkuk.thip.room.application.port.in.dto.RoomReportResult;

public record RoomReportResponse(
        Long roomId,
        int reportCount
) {
    public static RoomReportResponse of(RoomReportResult roomReportResult) {
        return new RoomReportResponse(roomReportResult.roomId(), roomReportResult.reportCount());
    }
}
