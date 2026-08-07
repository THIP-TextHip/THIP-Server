package konkuk.thip.room.application.service;

import konkuk.thip.room.application.port.in.RoomReportUseCase;
import konkuk.thip.room.application.port.in.dto.RoomReportResult;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.domain.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomReportService implements RoomReportUseCase {

    private final RoomCommandPort roomCommandPort;

    @Override
    @Transactional
    public RoomReportResult reportRoom(Long roomId) {
        Room room = roomCommandPort.getByIdOrThrow(roomId);
        room.increaseReportCount();
        roomCommandPort.update(room);

        return RoomReportResult.of(room.getId(), room.getReportCount());
    }
}
