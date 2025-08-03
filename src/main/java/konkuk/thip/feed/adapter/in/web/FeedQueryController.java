package konkuk.thip.feed.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.feed.adapter.in.web.response.*;
import konkuk.thip.feed.application.port.in.FeedShowAllUseCase;
import konkuk.thip.feed.application.port.in.FeedShowAllOfUserUseCase;
import konkuk.thip.feed.application.port.in.FeedShowSingleUseCase;
import konkuk.thip.feed.application.port.in.FeedShowUserInfoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.SHOW_SINGLE_FEED;

@Tag(name = "Feed Query API", description = "피드 조회 관련 API")
@RestController
@RequiredArgsConstructor
public class FeedQueryController {

    private final FeedShowAllUseCase feedShowAllUseCase;
    private final FeedShowAllOfUserUseCase feedShowMineUseCase;
    private final FeedShowUserInfoUseCase feedShowUserInfoUseCase;
    private final FeedShowSingleUseCase feedShowSingleUseCase;

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
            description = "내가 작성한 피드를 전체 조회합니다."
    )
    @GetMapping("/feeds/mine")
    public BaseResponse<FeedShowMineResponse> showMyFeeds(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "커서 (첫번째 요청시 : null, 다음 요청시 : 이전 요청에서 반환받은 nextCursor 값)")
            @RequestParam(value = "cursor", required = false) final String cursor) {
        return BaseResponse.ok(feedShowMineUseCase.showMyFeeds(userId, cursor));
    }

    @Operation(
            summary = "특정 유저의 공개 피드 조회",
            description = "내가 아닌 다른 유저가 작성한 공개 피드를 전체 조회합니다."
    )
    @GetMapping("/feeds/users/{userId}")
    public BaseResponse<FeedShowByUserResponse> showSpecificUserFeeds(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "해당 유저(= 피드 주인)의 userId 값") @PathVariable("userId") final Long feedOwnerId,
            @Parameter(description = "커서 (첫번째 요청시 : null, 다음 요청시 : 이전 요청에서 반환받은 nextCursor 값)")
            @RequestParam(value = "cursor", required = false) final String cursor) {
        return BaseResponse.ok(feedShowMineUseCase.showPublicFeedsOfFeedOwner(userId, feedOwnerId, cursor));
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
            summary = "특정 유저의 공개 피드 조회의 상단 화면 구성",
            description = "사용자의 정보, 사용자의 팔로워 정보, 사용자가 작성한 공개 피드 개수를 조회합니다."
    )
    @GetMapping("/feeds/users/{userId}/info")
    public BaseResponse<FeedShowUserInfoResponse> showAnotherUserInfoInFeeds(
            @Parameter(description = "피드 조회할 유저의 userId 값") @PathVariable final Long userId
    ) {
        return BaseResponse.ok(feedShowUserInfoUseCase.showAnotherUserInfoInFeeds(userId));
    }

    @Operation(
            summary = "피드 상세보기",
            description = "피드 하나의 본문 내용, 태그 목록 등을 조회합니다."
    )
    @ExceptionDescription(SHOW_SINGLE_FEED)
    @GetMapping("/feeds/{feedId}")
    public BaseResponse<FeedShowSingleResponse> showSingleFeed(
            @Parameter(description = "조회할 피드의 id값") @PathVariable final Long feedId,
            @Parameter(hidden = true) @UserId final Long userId
    ) {
        return BaseResponse.ok(feedShowSingleUseCase.showSingleFeed(feedId, userId));
    }
}
