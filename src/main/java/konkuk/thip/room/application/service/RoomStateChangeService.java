package konkuk.thip.room.application.service;

import konkuk.thip.message.application.port.out.RoomEventCommandPort;
import konkuk.thip.room.application.port.in.RoomStateChangeUseCase;
import konkuk.thip.room.application.port.out.RoomCommandPort;
import konkuk.thip.room.application.port.out.RoomParticipantCommandPort;
import konkuk.thip.room.domain.Room;
import konkuk.thip.room.domain.RoomParticipant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomStateChangeService implements RoomStateChangeUseCase {

    private final RoomCommandPort roomCommandPort;
    private final RoomParticipantCommandPort roomParticipantCommandPort;

    private final RoomEventCommandPort roomEventCommandPort;

    /**
     * end_date < 오늘 => EXPIRED
     */
    @Async("schedulerAsyncExecutor")
    @Override
    @Transactional
    public void changeRoomStateToExpired() {
        int updated = roomCommandPort.updateRoomStateToExpired();
        log.info("[RoomState] EXPIRED로 변경된 건수={}", updated);
    }

    /**
     * start_date <= 오늘 AND end_date >= 오늘 => IN_PROGRESS
     */
    @Async("schedulerAsyncExecutor")
    @Override
    @Transactional
    public void changeRoomStateToProgress() {
        // 방 모임방 활동 시작 푸쉬알림 전송
        sendNotifications();

        int updated = roomCommandPort.updateRoomStateFromRecruitingToProgress();
        log.info("[RoomState] IN_PROGRESS로 변경된 건수={}", updated);
    }

    private void sendNotifications() {
        List<Room> targetRooms = roomCommandPort.findProgressTargetRooms();
        for (Room room : targetRooms) {
            List<RoomParticipant> targetUsers = roomParticipantCommandPort.findAllByRoomId(room.getId());
            for (RoomParticipant participant : targetUsers) {
                roomEventCommandPort.publishRoomActivityStartedEvent(participant.getUserId(), room.getId(), room.getTitle());
            }
        }
    }
}
