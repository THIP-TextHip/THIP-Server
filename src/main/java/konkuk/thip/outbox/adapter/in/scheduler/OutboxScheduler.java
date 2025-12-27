package konkuk.thip.outbox.adapter.in.scheduler;

import konkuk.thip.outbox.application.port.in.FollowingOutboxProcessUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final FollowingOutboxProcessUseCase followingOutboxProcessUseCase;

    // 1초마다 PENDING 이벤트를 최대 100개씩 처리하는 예시
    @Scheduled(fixedDelay = 1000)
    public void processPendingEvents() {
        followingOutboxProcessUseCase.processFollowingOutboxEvents();
    }
}
