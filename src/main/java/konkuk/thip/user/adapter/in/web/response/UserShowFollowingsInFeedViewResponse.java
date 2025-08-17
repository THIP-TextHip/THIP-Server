package konkuk.thip.user.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;

@Schema(description = "전체 피드 조회 상단에서의 내 띱 목록 응답 DTO")
public record UserShowFollowingsInFeedViewResponse(
        @Schema(description = "내가 팔로잉하는 사람들 목록. 최근에 공개 피드를 작성한 사람들을 우선적으로 반환하고, 동일할 경우 최근에 팔로잉한 순으로 반환합니다.")
        List<UserShowFollowingsInFeedViewDto> myFollowingUsers
) {
    public record UserShowFollowingsInFeedViewDto(
            @Schema(description = "내가 팔로잉하는 사람의 userId 값")
            Long userId,

            @Schema(description = "내가 팔로잉하는 사람의 nickname 값")
            String nickname,

            @Schema(description = "내가 팔로잉하는 사람의 profileImageUrl 값")
            String profileImageUrl
    ) { }

    public static UserShowFollowingsInFeedViewResponse returnEmptyList() {
        return new UserShowFollowingsInFeedViewResponse(Collections.emptyList());     // 빈 리스트 반환
    }
}
