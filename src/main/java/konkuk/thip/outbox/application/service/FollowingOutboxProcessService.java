package konkuk.thip.outbox.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.outbox.adapter.out.jpa.OutboxEventJpaEntity;
import konkuk.thip.outbox.adapter.out.jpa.OutboxEventType;
import konkuk.thip.outbox.adapter.out.jpa.OutboxStatus;
import konkuk.thip.outbox.application.port.in.FollowingOutboxProcessUseCase;
import konkuk.thip.outbox.application.port.out.OutboxEventPersistencePort;
import konkuk.thip.user.adapter.out.event.dto.FollowingEvent;
import konkuk.thip.user.application.port.out.UserCommandPort;
import konkuk.thip.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowingOutboxProcessService implements FollowingOutboxProcessUseCase {

    private final OutboxEventPersistencePort outboxEventPersistencePort;
    private final ObjectMapper objectMapper;

    private final UserCommandPort userCommandPort;

    @Async("outboxAsyncExecutor")
    @Override
    @Transactional
    public void processFollowingOutboxEvents() {
        List<OutboxEventJpaEntity> events = outboxEventPersistencePort.findTop1000ByStatusOrderByIdAsc(OutboxStatus.PENDING);

        for (OutboxEventJpaEntity event : events) {
            try {
                if (OutboxEventType.USER_FOLLOWED.equals(event.getEventType())) {
                    FollowingEvent.UserFollowedEvent payload =
                            objectMapper.readValue(event.getPayload(), FollowingEvent.UserFollowedEvent.class);

                    User user = userCommandPort.findById(payload.targetUserId());
                    user.increaseFollowerCount();
                    userCommandPort.update(user);
                } else if (OutboxEventType.USER_UNFOLLOWED.equals(event.getEventType())) {
                    FollowingEvent.UserFollowedEvent payload =
                            objectMapper.readValue(event.getPayload(), FollowingEvent.UserFollowedEvent.class);
                    User user = userCommandPort.findById(payload.targetUserId());
                    user.decreaseFollowerCount();
                    userCommandPort.update(user);
                }

                event.markAsProcessed();
            } catch (Exception e) {
                // 실패 시 전략: FAILED로 마킹 + 로그, 재시도 정책 등
                event.markAsFailed();
            }
        }
    }
}
