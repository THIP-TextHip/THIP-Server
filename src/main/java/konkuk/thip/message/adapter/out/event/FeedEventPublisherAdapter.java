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
    public void publishFollowEvent(
            String title, String content, Long notificationId,
            Long targetUserId) {
        publisher.publishEvent(FeedEvents.FollowerEvent.builder()
                .title(title)
                .content(content)
                .notificationId(notificationId)
                .targetUserId(targetUserId)
                .build());
    }

    @Override
    public void publishFeedCommentedEvent(
            String title, String content, Long notificationId,
            Long targetUserId) {
        publisher.publishEvent(FeedEvents.FeedCommentedEvent.builder()
                .title(title)
                .content(content)
                .notificationId(notificationId)
                .targetUserId(targetUserId)
                .build());
    }

    @Override
    public void publishFeedRepliedEvent(
            String title, String content, Long notificationId,
            Long targetUserId) {
        publisher.publishEvent(FeedEvents.FeedCommentRepliedEvent.builder()
                .title(title)
                .content(content)
                .notificationId(notificationId)
                .targetUserId(targetUserId)
                .build());
    }

    @Override
    public void publishFolloweeNewFeedEvent(
            String title, String content, Long notificationId,
            Long targetUserId) {
        publisher.publishEvent(FeedEvents.FolloweeNewFeedEvent.builder()
                .title(title)
                .content(content)
                .notificationId(notificationId)
                .targetUserId(targetUserId)
                .build());
    }

    @Override
    public void publishFeedLikedEvent(
            String title, String content, Long notificationId,
            Long targetUserId) {
        publisher.publishEvent(FeedEvents.FeedLikedEvent.builder()
                .title(title)
                .content(content)
                .notificationId(notificationId)
                .targetUserId(targetUserId)
                .build());
    }

    @Override
    public void publishFeedCommentLikedEvent(
            String title, String content, Long notificationId,
            Long targetUserId) {
        publisher.publishEvent(FeedEvents.FeedCommentLikedEvent.builder()
                .title(title)
                .content(content)
                .notificationId(notificationId)
                .targetUserId(targetUserId)
                .build());
    }
}
