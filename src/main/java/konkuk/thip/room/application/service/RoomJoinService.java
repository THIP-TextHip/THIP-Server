package konkuk.thip.room.application.service;

import konkuk.thip.common.exception.EntityNotFoundException;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.room.application.port.in.RoomJoinUseCase;
import konkuk.thip.room.application.port.in.dto.RoomJoinCommand;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantQueryPort;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.domain.RoomJoinType;
import konkuk.thip.room.domain.RoomParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static konkuk.thip.room.adapter.out.jpa.RoomParticipantRole.MEMBER;

@Service
@RequiredArgsConstructor
public class RoomJoinService implements RoomJoinUseCase {

    private final RoomParticipantQueryPort roomParticipantQueryPort;
    private final RoomCommandPort roomCommandPort;
    private final RoomParticipantCommandPort roomParticipantCommandPort;

    @Override
    @Transactional
    public void changeJoinState(RoomJoinCommand roomJoinCommand) {
        RoomJoinType type = RoomJoinType.from(roomJoinCommand.type());

        // 방이 존재하지 않거나 만료된 경우
        Room room;
        try {
            room = roomCommandPort.findById(roomJoinCommand.roomId());
        } catch (EntityNotFoundException e) {
            throw new InvalidStateException(ErrorCode.USER_CANNOT_JOIN_OR_CANCEL);
        }

        boolean isParticipate = roomParticipantQueryPort.existByUserIdAndRoomId(roomJoinCommand.userId(), roomJoinCommand.roomId());
        room.validateRoomExpired();

        // 참여하기 요청
        if(type.isJoinType()) {
            // 이미 참여한 상태
            if(isParticipate) {
                throw new InvalidStateException(ErrorCode.USER_ALREADY_PARTICIPATE);
            }

            RoomParticipant roomParticipant = RoomParticipant.withoutId(roomJoinCommand.userId(), roomJoinCommand.roomId(), MEMBER.getType());
            roomParticipantCommandPort.save(roomParticipant);

            //Room의 memberCount 업데이트
            room.increaseMemberCount();
        }

        // 취소하기 요청
        if(!type.isJoinType()) {
            // 참여하지 않은 상태
            if(!isParticipate) {
                throw new InvalidStateException(ErrorCode.USER_NOT_PARTICIPATED_CANNOT_CANCEL);
            }

            // 방장이 참여 취소를 요청한 경우
            RoomParticipant roomParticipant = roomParticipantCommandPort.findByUserIdAndRoomId(roomJoinCommand.userId(), roomJoinCommand.roomId());
            roomParticipant.cancelParticipation();

            roomParticipantCommandPort.deleteByUserIdAndRoomId(roomJoinCommand.userId(), roomJoinCommand.roomId());

            //Room의 memberCount 업데이트
            room.decreaseMemberCount();
        }

        // 방의 상태 업데이트
        roomCommandPort.updateMemberCount(room);
    }


}
