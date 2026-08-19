package konkuk.thip.user.adapter.in.web.response;

import lombok.Builder;

import java.util.List;

@Builder
public record UserBlockedListResponse(
        List<BlockedUserDto> blockedUsers,
        Integer totalBlockedUserCount,
        String nextCursor,
        boolean isLast
) {
    @Builder
    public record BlockedUserDto(
            Long userId,
            String nickname,
            String profileImageUrl,
            String aliasName,
            String aliasColor
    ) {
    }
}
