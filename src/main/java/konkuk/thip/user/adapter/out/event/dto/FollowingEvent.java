package konkuk.thip.user.adapter.out.event.dto;

import lombok.Builder;

public class FollowingEvent {
    @Builder
    public record UserFollowedEvent(Long userId, Long targetUserId) {}
    @Builder
    public record UserUnfollowedEvent(Long userId, Long targetUserId) {}
}
