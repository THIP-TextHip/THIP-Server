package konkuk.thip.room.application.service;

import jakarta.persistence.LockTimeoutException;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.notification.application.port.in.RoomNotificationOrchestrator;
import konkuk.thip.room.application.port.in.RoomJoinUseCase;
import konkuk.thip.room.application.port.in.dto.RoomJoinCommand;
import konkuk.thip.room.application.port.in.dto.RoomJoinResult;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.application.port.in.dto.RoomJoinType;
import konkuk.thip.room.domain.RoomParticipant;
import konkuk.thip.user.application.port.out.UserBlockQueryPort;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomJoinService implements RoomJoinUseCase {

    private final RoomCommandPort roomCommandPort;
    private final RoomParticipantCommandPort roomParticipantCommandPort;
    private final UserCommandPort userCommandPort;
    private final UserBlockQueryPort userBlockQueryPort;

    private final RoomNotificationOrchestrator roomNotificationOrchestrator;

    @Override
    @Retryable(
            retryFor = {
                    LockTimeoutException.class
            },  // 재시도 대상 예외
            noRetryFor = {
                    InvalidStateException.class, BusinessException.class
            },  // 제시도 제외 예외
            maxAttempts = 2,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)  // 재시도마다 새로운 트랜잭션
    public RoomJoinResult changeJoinState(RoomJoinCommand roomJoinCommand) {
        RoomJoinType type = roomJoinCommand.type();

        // 방이 존재하지 않거나 모집기간이 만료된 경우 예외 처리
//        Room room = roomCommandPort.findById(roomJoinCommand.roomId())
//                .orElseThrow(() -> new BusinessException(ErrorCode.USER_CANNOT_JOIN_OR_CANCEL));

        /** x-lock 획득하여 room 조회 **/
        Room room = roomCommandPort.getByIdForUpdate(roomJoinCommand.roomId());

        room.validateRoomRecruitExpired();

        Optional<RoomParticipant> roomParticipantOptional = roomParticipantCommandPort.findByUserIdAndRoomIdOptional(roomJoinCommand.userId(), roomJoinCommand.roomId());

        // 참여하려는 방의 방장이 차단 관계면 참여를 막는다. 나가기(CANCEL)에는 적용하지 않는다.
        if (type == RoomJoinType.JOIN) {
            validateHostNotBlocked(roomJoinCommand.userId(), room.getId());
        }

        // 방 참여 상태 변경 요청에 따라 분기 처리
        switch (type) {
            case JOIN -> handleJoin(roomJoinCommand, roomParticipantOptional, room);
            case CANCEL -> handleCancel(roomJoinCommand, roomParticipantOptional, room);
        }

        // 방의 상태 업데이트
        roomCommandPort.update(room);

        // 참여자 푸쉬 알림 전송 (호스트에게만 전송)
        if (type == RoomJoinType.JOIN) {
            sendNotifications(roomJoinCommand, room);
        }

        return RoomJoinResult.of(room.getId(), type.getType());
    }

    @Recover
    public RoomJoinResult recoverLockTimeout(LockTimeoutException e, RoomJoinCommand roomJoinCommand) {
        throw new BusinessException(ErrorCode.RESOURCE_LOCKED);
    }

    @Recover
    public RoomJoinResult recoverInvalidStateException(InvalidStateException e, RoomJoinCommand roomJoinCommand) {
        throw e;
    }

    @Recover
    public RoomJoinResult recoverBusinessException(BusinessException e, RoomJoinCommand roomJoinCommand) {
        throw e;
    }

    private void validateHostNotBlocked(Long userId, Long roomId) {
        RoomParticipant host = roomParticipantCommandPort.findHostByRoomId(roomId);
        if (host != null && userBlockQueryPort.existsBlockBetween(userId, host.getUserId())) {
            throw new BusinessException(ErrorCode.ROOM_HOST_BLOCKED);
        }
    }

    private void sendNotifications(RoomJoinCommand roomJoinCommand, Room room) {
        RoomParticipant targetUser = roomParticipantCommandPort.findHostByRoomId(room.getId());
        User actorUser = userCommandPort.findById(roomJoinCommand.userId());
        roomNotificationOrchestrator.notifyRoomJoinToHost(targetUser.getUserId(), room.getId(), room.getTitle(), actorUser.getId(), actorUser.getNickname());
    }

    private void handleCancel(RoomJoinCommand roomJoinCommand, Optional<RoomParticipant> participantOptional, Room room) {
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

    // 방장이 참여 취소를 요청한 경우
    private void validateCancelable(RoomParticipant roomParticipant) {
        if (roomParticipant.isHost()) {
            throw new BusinessException(ErrorCode.HOST_CANNOT_CANCEL);
        }
    }
}
