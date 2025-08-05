package konkuk.thip.room.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.room.application.port.in.RoomRecruitCloseUseCase;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.domain.RoomParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomRecruitCloseService implements RoomRecruitCloseUseCase {

    private final RoomParticipantCommandPort roomParticipantCommandPort;
    private final RoomCommandPort roomCommandPort;

    //todo 모집 마감시 방 참여자들에게 모집 마감 알림 전송
    @Override
    @Transactional
    public Long closeRoomRecruit(Long userId, Long roomId) {
        // 1. 방 참여자 조회
        RoomParticipant roomParticipant = roomParticipantCommandPort.findByUserIdAndRoomIdOptional(userId, roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_RECRUIT_CANNOT_CLOSED,
                        new IllegalArgumentException("사용자가 방에 참여하지 않은 상태입니다.")));

        // 2. 방 멤버는 방 모집 마감 불가
        validateCloseable(roomParticipant);

        // 3. 모집 마감시 방 시작일을 현재 시간으로 변경
        Room room = roomCommandPort.getByIdOrThrow(roomId);
        room.startRoomProgress();

        // 4. Room 테이블 업데이트
        roomCommandPort.update(room);

        return room.getId();
    }


    private void validateCloseable(RoomParticipant roomParticipant) {
        if (roomParticipant.isMember()) {
            throw new BusinessException(ErrorCode.ROOM_RECRUIT_CANNOT_CLOSED,
                    new IllegalArgumentException("사용자는 방의 멤버입니다. 오직 호스트만 모집 마감이 가능합니다."));
        }
    }
}
