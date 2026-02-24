package konkuk.thip.user.adapter.out.event.dto;

import java.util.List;

public record UserWithdrawnEvent(Long userId, List<Long> deletedFeedIds) {
    public static UserWithdrawnEvent of(Long userId, List<Long> deletedFeedIds) {
        return new UserWithdrawnEvent(userId, deletedFeedIds);
    }
}