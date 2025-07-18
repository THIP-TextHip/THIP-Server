package konkuk.thip.room.application.service;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.room.application.port.in.RoomRecruitCloseUsecase;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.domain.RoomParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomRecruitCloseService implements RoomRecruitCloseUsecase {

    private final RoomParticipantCommandPort roomParticipantCommandPort;
    private final RoomCommandPort roomCommandPort;

    //todo 모집 마감시 방 참여자들에게 모집 마감 알림 전송
    @Override
    public void closeRoomJoin(Long userId, Long roomId) {
        //호스트만 모집마감 가능
        RoomParticipant roomParticipant;
        try {
            roomParticipant = roomParticipantCommandPort.findByUserIdAndRoomId(userId, roomId);
        } catch (EntityNotFoundException e) {
            throw new InvalidStateException(ErrorCode.USER_NOT_PARTICIPATED_CANNOT_CLOSE);
        }

        roomParticipant.validateMemberCloseRoom();

        // 모집 마감시 방 시작일을 현재 시간으로 변경
        Room room = roomCommandPort.findById(roomId);


    }
}
