package konkuk.thip.outbox.adapter.in.event;

import konkuk.thip.outbox.application.port.in.FollowingDispatchUseCase;
import konkuk.thip.user.adapter.out.event.dto.FollowingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FollowingEventListener {

    private final FollowingDispatchUseCase followingDispatchUseCase;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onUserFollowed(FollowingEvent.UserFollowedEvent event) {
        followingDispatchUseCase.handleUserFollow(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onUserUnfollowed(FollowingEvent.UserUnfollowedEvent event) {
        followingDispatchUseCase.handleUserUnfollow(event);
    }
}