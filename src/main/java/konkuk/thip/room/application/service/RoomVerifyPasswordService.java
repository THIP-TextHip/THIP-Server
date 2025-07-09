package konkuk.thip.room.application.service;

import konkuk.thip.room.application.port.in.RoomVerifyPasswordUseCase;
import konkuk.thip.room.application.port.in.dto.RoomVerifyPasswordQuery;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.domain.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomVerifyPasswordService implements RoomVerifyPasswordUseCase {

    private final RoomCommandPort roomCommandPort;

    @Override
    public Void verifyRoomPassword(RoomVerifyPasswordQuery query) {

        //방 검증
        Room room = roomCommandPort.findById(query.roomId());

        //도메인에서 비밀번호 검증 로직 수행
        room.verifyPassword(query.password());
        return null;
    }
}
