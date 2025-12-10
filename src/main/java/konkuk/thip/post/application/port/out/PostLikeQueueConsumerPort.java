package konkuk.thip.post.application.port.out;

import java.util.Optional;
import konkuk.thip.post.application.port.out.dto.PostLikeQueueMessage;

public interface PostLikeQueueConsumerPort {
    Optional<PostLikeQueueMessage> consumeOne();
}
