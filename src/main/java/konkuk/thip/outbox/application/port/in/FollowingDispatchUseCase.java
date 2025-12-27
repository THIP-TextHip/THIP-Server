package konkuk.thip.outbox.application.port.in;

import konkuk.thip.user.adapter.out.event.dto.FollowingEvent;

public interface FollowingDispatchUseCase {
    void handleUserFollow(FollowingEvent.UserFollowedEvent e);
    void handleUserUnfollow(FollowingEvent.UserUnfollowedEvent e);
}
