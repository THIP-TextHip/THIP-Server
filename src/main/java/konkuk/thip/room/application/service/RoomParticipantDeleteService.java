package konkuk.thip.room.application.service;

import konkuk.thip.room.application.port.in.RoomParticipantDeleteUseCase;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.domain.RoomParticipant;
import konkuk.thip.roompost.application.service.manager.RoomProgressManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomParticipantDeleteService implements RoomParticipantDeleteUseCase {

    private final RoomCommandPort roomCommandPort;
    private final RoomParticipantCommandPort roomParticipantCommandPort;

    private final RoomProgressManager roomProgressManager;

    @Override
    @Transactional
    public Void leaveRoom(Long userId, Long roomId) {

        // 1. 방 조회 및 검증
        Room room = roomCommandPort.getByIdOrThrow(roomId);

        // 2. 사용자가 방 참여자인지 확인
        RoomParticipant roomParticipant = roomParticipantCommandPort.getByUserIdAndRoomIdOrThrow(userId, room.getId());
        // 2-1. 방 나가기 권한 검증
        roomParticipant.validateRoomLeavable();

        // 3. 방 멤버수 감소 / 방 진행률 업데이트
        roomProgressManager.removeUserProgressAndUpdateRoomProgress(roomParticipant.getId(), room.getId());

        // 4. 방나가기
        roomParticipantCommandPort.deleteByUserIdAndRoomId(userId, room.getId());
        return null;
    }
}
