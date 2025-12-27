package konkuk.thip.user.adapter.out.event;

import konkuk.thip.user.adapter.out.event.dto.FollowingEvent;
import konkuk.thip.user.application.port.out.FollowingEventCommandPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowingEventPublisherAdapter implements FollowingEventCommandPort {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publishUserFollowedEvent(Long userId, Long targetUserId) {
        publisher.publishEvent(
                FollowingEvent.UserFollowedEvent.builder()
                        .userId(userId)
                        .targetUserId(targetUserId)
                        .build()
        );
    }

    @Override
    public void publishUserUnfollowedEvent(Long userId, Long targetUserId) {
        publisher.publishEvent(
                FollowingEvent.UserUnfollowedEvent.builder()
                        .userId(userId)
                        .targetUserId(targetUserId)
                        .build()
        );
    }
}
