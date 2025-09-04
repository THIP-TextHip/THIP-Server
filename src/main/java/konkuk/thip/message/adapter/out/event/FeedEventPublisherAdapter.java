package konkuk.thip.message.adapter.out.event;

import konkuk.thip.message.adapter.out.event.dto.FeedEvents;
import konkuk.thip.message.application.port.out.FeedEventCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedEventPublisherAdapter implements FeedEventCommandPort {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publishFollowEvent(Long targetUserId, Long actorUserId, String actorUsername) {
        publisher.publishEvent(FeedEvents.FollowerEvent.builder()
                .targetUserId(targetUserId)
                .actorUserId(actorUserId)
                .actorUsername(actorUsername)
                .build());
    }

    @Override
    public void publishFeedCommentedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                          Long feedId) {
        publisher.publishEvent(FeedEvents.FeedCommentedEvent.builder()
                .targetUserId(targetUserId)
                .actorUserId(actorUserId)
                .actorUsername(actorUsername)
                .feedId(feedId)
                .build());
    }

    @Override
    public void publishFeedRepliedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                        Long feedId) {
        publisher.publishEvent(FeedEvents.FeedRepliedEvent.builder()
                .targetUserId(targetUserId)
                .actorUserId(actorUserId)
                .actorUsername(actorUsername)
                .feedId(feedId)
                .build());
    }

    @Override
    public void publishFolloweeNewPostEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                            Long feedId) {
        publisher.publishEvent(FeedEvents.FolloweeNewPostEvent.builder()
                .targetUserId(targetUserId)
                .actorUserId(actorUserId)
                .actorUsername(actorUsername)
                .feedId(feedId)
                .build());
    }

    @Override
    public void publishFeedLikedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                      Long feedId) {
        publisher.publishEvent(FeedEvents.FeedLikedEvent.builder()
                .targetUserId(targetUserId)
                .actorUserId(actorUserId)
                .actorUsername(actorUsername)
                .feedId(feedId)
                .build());
    }

    @Override
    public void publishFeedCommentLikedEvent(Long targetUserId, Long actorUserId, String actorUsername,
                                             Long feedId) {
        publisher.publishEvent(FeedEvents.FeedCommentLikedEvent.builder()
                .targetUserId(targetUserId)
                .actorUserId(actorUserId)
                .actorUsername(actorUsername)
                .feedId(feedId)
                .build());
    }
}