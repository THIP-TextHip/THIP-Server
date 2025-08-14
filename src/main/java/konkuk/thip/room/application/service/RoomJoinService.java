package konkuk.thip.room.application.service;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.room.application.port.in.RoomJoinUseCase;
import konkuk.thip.room.application.port.in.dto.RoomJoinCommand;
import konkuk.thip.room.application.port.in.dto.RoomJoinResult;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.domain.RoomJoinType;
import konkuk.thip.room.domain.RoomParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static konkuk.thip.room.adapter.out.jpa.RoomParticipantRole.MEMBER;

@Service
@RequiredArgsConstructor
public class RoomJoinService implements RoomJoinUseCase {

    private final RoomCommandPort roomCommandPort;
    private final RoomParticipantCommandPort roomParticipantCommandPort;

    @Override
    @Transactional
    public RoomJoinResult changeJoinState(RoomJoinCommand roomJoinCommand) {
        RoomJoinType type = RoomJoinType.from(roomJoinCommand.type());

        // 방이 존재하지 않거나 만료된 경우
        Room room = roomCommandPort.findById(roomJoinCommand.roomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_CANNOT_JOIN_OR_CANCEL));

        validateRoom(room);

        Optional<RoomParticipant> roomParticipantOptional = roomParticipantCommandPort.findByUserIdAndRoomIdOptional(roomJoinCommand.userId(), roomJoinCommand.roomId());

        // 참여하기 요청
        if(type.isJoinType()) {
            handleJoin(roomJoinCommand, roomParticipantOptional, room);
        }

        // 취소하기 요청
        if(!type.isJoinType()) {
            handleCancel(roomJoinCommand, roomParticipantOptional, roomParticipantOptional, room);
        }

        // 방의 상태 업데이트
        roomCommandPort.update(room);

        return RoomJoinResult.of(room.getId(), roomJoinCommand.type());
    }

    private void handleCancel(RoomJoinCommand roomJoinCommand, Optional<RoomParticipant> participantOptional, Optional<RoomParticipant> roomParticipantOptional, Room room) {
        // 참여하지 않은 상태
        RoomParticipant participant = participantOptional.orElseThrow(() ->
                new BusinessException(ErrorCode.USER_NOT_PARTICIPATED_CANNOT_CANCEL)
        );

        // 방장은 참여 취소를 할 수 없음
        validateCancelable(participant);

        roomParticipantCommandPort.deleteByUserIdAndRoomId(roomJoinCommand.userId(), roomJoinCommand.roomId());

        //Room의 memberCount 업데이트
        room.decreaseMemberCount();
    }

    private void handleJoin(RoomJoinCommand roomJoinCommand, Optional<RoomParticipant> participantOptional, Room room) {
        // 이미 참여한 상태
        participantOptional.ifPresent(p -> {
            throw new BusinessException(ErrorCode.USER_ALREADY_PARTICIPATE);
        });

        RoomParticipant roomParticipant = RoomParticipant.memberWithoutId(roomJoinCommand.userId(), roomJoinCommand.roomId());
        roomParticipantCommandPort.save(roomParticipant);

        //Room의 memberCount 업데이트
        room.increaseMemberCount();
    }

    private void validateRoom(Room room) {
        room.validateRoomRecruitExpired();
        room.validateRoomExpired();
    }

    // 방장이 참여 취소를 요청한 경우
    private void validateCancelable(RoomParticipant roomParticipant) {
        if (roomParticipant.isHost()) {
            throw new BusinessException(ErrorCode.HOST_CANNOT_CANCEL);
        }
    }


}
