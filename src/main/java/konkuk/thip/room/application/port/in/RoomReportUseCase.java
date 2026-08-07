package konkuk.thip.room.application.port.in;

import konkuk.thip.room.application.port.in.dto.RoomReportResult;

public interface RoomReportUseCase {
    RoomReportResult reportRoom(Long roomId);
}
