package konkuk.thip.post.application.port.out;

import konkuk.thip.post.adapter.out.event.dto.PostLikeChangedEvent;

public interface PostLikeQueueCommandPort {
    void enqueueFromEvent(PostLikeChangedEvent event);
}