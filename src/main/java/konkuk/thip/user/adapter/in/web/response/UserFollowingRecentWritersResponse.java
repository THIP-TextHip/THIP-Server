package konkuk.thip.user.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "전체 피드 조회 상단에서의 내 띱 목록 응답 DTO")
public record UserFollowingRecentWritersResponse(
        @Schema(description = "내가 팔로잉하는 사람들 중, 최근에 공개 피드를 작성한 사람들")
        List<RecentWriter> recentWriters
) {
    public record RecentWriter(
            @Schema(description = "최근에 피드를 작성한 사람의 userId 값")
            Long userId,

            @Schema(description = "최근에 피드를 작성한 사람의 nickname 값")
            String nickname,

            @Schema(description = "최근에 피드를 작성한 사람의 profileImageUrl 값")
            String profileImageUrl
    ) {}
}
