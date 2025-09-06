package konkuk.thip.common.scheduler;

import konkuk.thip.room.application.port.in.RoomStateChangeUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomStateScheduler {

    private final RoomStateChangeUseCase roomStateChangeUseCase;

    // 매일 자정 실행
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void changeRoomState() {
        log.info("[스케줄러] 방 상태 변경 시작");
        roomStateChangeUseCase.changeRoomStateToExpired();
        roomStateChangeUseCase.changeRoomStateToProgress();
        log.info("[스케줄러] 방 상태 변경 완료");
    }
}
