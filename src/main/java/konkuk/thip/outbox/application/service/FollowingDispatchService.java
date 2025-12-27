package konkuk.thip.outbox.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.code.ErrorCode;
import konkuk.thip.user.adapter.out.event.dto.FollowingEvent;
import konkuk.thip.outbox.adapter.out.jpa.OutboxEventJpaEntity;
import konkuk.thip.outbox.adapter.out.jpa.OutboxEventType;
import konkuk.thip.outbox.application.port.in.FollowingDispatchUseCase;
import konkuk.thip.outbox.application.port.out.OutboxEventPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowingDispatchService implements FollowingDispatchUseCase {

    private final OutboxEventPersistencePort outboxEventPersistencePort;
    private final ObjectMapper objectMapper;

    @Override
    public void handleUserFollow(FollowingEvent.UserFollowedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEventJpaEntity entity = OutboxEventJpaEntity.pending(
                    "USER",
                    event.targetUserId(),
                    OutboxEventType.USER_FOLLOWED,
                    payload
            );
            outboxEventPersistencePort.save(entity);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR);
        }
    }

    @Override
    public void handleUserUnfollow(FollowingEvent.UserUnfollowedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEventJpaEntity entity = OutboxEventJpaEntity.pending(
                    "USER",
                    event.targetUserId(),
                    OutboxEventType.USER_UNFOLLOWED,
                    payload
            );
            outboxEventPersistencePort.save(entity);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.JSON_PROCESSING_ERROR);
        }
    }
}
