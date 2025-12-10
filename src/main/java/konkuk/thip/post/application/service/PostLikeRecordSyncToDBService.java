package konkuk.thip.post.application.service;

import java.util.Optional;
import konkuk.thip.post.application.port.out.PostLikeCommandPort;
import konkuk.thip.post.application.port.out.PostLikeQueueConsumerPort;
import konkuk.thip.post.application.port.out.dto.PostLikeQueueMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PostLikeRecordSyncToDBService {

    private final PostLikeCommandPort postLikeCommandPort;
    private final PostLikeQueueConsumerPort postLikeQueueConsumerPort;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void syncRecordsFromQueue() {
        Optional<PostLikeQueueMessage> messageOptional = postLikeQueueConsumerPort.consumeOne();

        while (messageOptional.isPresent()) {
            PostLikeQueueMessage command = messageOptional.get();

            // DB에 실제 INSERT/DELETE 작업 수행
            if ("SAVE".equals(command.action())) {
                postLikeCommandPort.save(command.userId(), command.postId(), command.postType());
            } else if ("DELETE".equals(command.action())) {
                postLikeCommandPort.delete(command.userId(), command.postId());
            }

            // 다음 메시지 가져오기
            messageOptional = postLikeQueueConsumerPort.consumeOne();
        }
    }
}