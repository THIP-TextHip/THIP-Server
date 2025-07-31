package konkuk.thip.feed.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.feed.adapter.in.web.response.FeedShowUserInfoResponse;
import konkuk.thip.feed.adapter.in.web.response.FeedShowMineResponse;
import konkuk.thip.feed.adapter.in.web.response.FeedShowAllResponse;
import konkuk.thip.feed.application.port.in.FeedShowAllUseCase;
import konkuk.thip.feed.application.port.in.FeedShowMineUseCase;
import konkuk.thip.feed.application.port.in.FeedShowUserInfoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Feed Query API", description = "피드 조회 관련 API")
@RestController
@RequiredArgsConstructor
public class FeedQueryController {

    private final FeedShowAllUseCase feedShowAllUseCase;
    private final FeedShowMineUseCase feedShowMineUseCase;
    private final FeedShowUserInfoUseCase feedShowUserInfoUseCase;

    @Operation(
            summary = "피드 전체 조회",
            description = "THIP을 사용하는 모든 유저가 작성한 피드를 전체 조회합니다."
    )
    @GetMapping("/feeds")
    public BaseResponse<FeedShowAllResponse> showAllFeeds(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "커서 (첫번째 요청시 : null, 다음 요청시 : 이전 요청에서 반환받은 nextCursor 값)")
            @RequestParam(value = "cursor", required = false) final String cursor) {
        return BaseResponse.ok(feedShowAllUseCase.showAllFeeds(userId, cursor));
    }

    @Operation(
            summary = "내 피드 조회",
            description = "사용자가 작성한 피드를 전체 조회합니다."
    )
    @GetMapping("/feeds/mine")
    public BaseResponse<FeedShowMineResponse> showMyFeeds(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "커서 (첫번째 요청시 : null, 다음 요청시 : 이전 요청에서 반환받은 nextCursor 값)")
            @RequestParam(value = "cursor", required = false) final String cursor) {
        return BaseResponse.ok(feedShowMineUseCase.showMyFeeds(userId, cursor));
    }

    @Operation(
            summary = "내 피드 조회의 상단 화면 구성",
            description = "사용자의 정보, 사용자의 팔로워 정보, 사용자가 작성한 전체 피드 개수를 조회합니다."
    )
    @GetMapping("/feeds/mine/info")
    public BaseResponse<FeedShowUserInfoResponse> showMyInfoInFeeds(@Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(feedShowUserInfoUseCase.showMyInfoInFeeds(userId));
    }

    @Operation(
            summary = "특정 유저 피드 조회의 상단 화면 구성",
            description = "사용자의 정보, 사용자의 팔로워 정보, 사용자가 작성한 공개 피드 개수를 조회합니다."
    )
    @GetMapping("/feeds/users/{userId}/info")
    public BaseResponse<FeedShowUserInfoResponse> showAnotherUserInfoInFeeds(
            @Parameter(description = "피드 조회할 유저의 userId 값") @PathVariable final Long userId
    ) {
        return BaseResponse.ok(feedShowUserInfoUseCase.showAnotherUserInfoInFeeds(userId));
    }
}
