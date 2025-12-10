package konkuk.thip.post.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import konkuk.thip.post.adapter.out.event.dto.PostLikeChangedEvent;
import konkuk.thip.post.application.port.out.PostLikeQueueCommandPort;
import konkuk.thip.post.application.port.out.PostLikeQueueConsumerPort;
import konkuk.thip.post.application.port.out.dto.PostLikeQueueMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostLikeQueueRedisAdapter implements PostLikeQueueCommandPort, PostLikeQueueConsumerPort {

    private final StringRedisTemplate stringRedisTemplate; 
    private final ObjectMapper objectMapper; 

    @Value("${app.redis.post-like-queue-prefix}")
    private String postLikeQueuePrefix;

    @Override
    public void enqueueFromEvent(PostLikeChangedEvent event) {
        PostLikeQueueMessage message = new PostLikeQueueMessage(
                event.userId(),
                event.postId(),
                event.isLike() ? "SAVE" : "DELETE",
                event.postType()
        );

        try {
            String jsonRecord = objectMapper.writeValueAsString(message);
            stringRedisTemplate.opsForList().leftPush(postLikeQueuePrefix, jsonRecord);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize and publish event to queue.", e); 
        }
    }

    @Override
    public Optional<PostLikeQueueMessage> consumeOne() {
        // BRPOP: 큐에 메시지가 있을 때까지 최대 1초 블로킹 대기
        String jsonRecord = stringRedisTemplate.opsForList().rightPop(postLikeQueuePrefix, Duration.ofSeconds(1));

        if (jsonRecord == null) {
            return Optional.empty();
        }

        try {
            PostLikeQueueMessage message = objectMapper.readValue(jsonRecord, PostLikeQueueMessage.class);
            return Optional.of(message);
        } catch (Exception e) {
            log.error("Failed to deserialize like record: {}", jsonRecord, e);
            // 디시리얼라이즈 실패 메시지는 무시하고 다음 메시지를 시도하거나, DLQ로 보내야 함.
            // 여기서는 유실을 가정하고 Optional.empty() 반환
            return Optional.empty();
        }
    }
}