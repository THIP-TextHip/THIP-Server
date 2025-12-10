package konkuk.thip.post.adapter.out.event;

import konkuk.thip.post.adapter.out.event.dto.PostLikeChangedEvent;
import konkuk.thip.post.application.port.out.PostLikeEventCommandPort;
import konkuk.thip.post.application.port.out.PostLikeQueueCommandPort;
import konkuk.thip.post.application.port.out.PostLikeRedisCommandPort;
import konkuk.thip.post.domain.PostType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostLikeEventSyncAdapter implements PostLikeEventCommandPort {

    private final ApplicationEventPublisher publisher;

    private final PostLikeRedisCommandPort postLikeRedisCommandPort;
    private final PostLikeQueueCommandPort postLikeQueueCommandPort;

    @Override
    public void publishEvent(Long userId, Long postId, boolean isLike, PostType postType, int finalLikeCount) {
        publisher.publishEvent(PostLikeChangedEvent.builder()
                .userId(userId)
                .postId(postId)
                .isLike(isLike)
                .postType(postType)
                .finalLikeCount(finalLikeCount)
                .build());
    }

    @Async("postLikeAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    protected void handlePostLikeChangedEvent(PostLikeChangedEvent event) {
        // 1. Redis Set 기록 (좋아요 상태 반영)
        if (event.isLike()) {
            postLikeRedisCommandPort.addLikeRecordToSet(event.userId(), event.postId());
        } else {
            postLikeRedisCommandPort.removeLikeRecordFromSet(event.userId(), event.postId());
        }

        // 2. Redis 카운트 갱신 (INCR/DECR)
        postLikeRedisCommandPort.updateLikeCount(event.postType(), event.postId(),
                event.finalLikeCount(), event.isLike());

        // 3. 비동기 DB 기록 메시지를 큐에 삽입 (Redis List LPUSH)
        try {
            postLikeQueueCommandPort.enqueueFromEvent(event);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to publish Like Record to Queue. Event Data: {}", event, e);
        }
    }
}
