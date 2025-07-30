package konkuk.thip.feed.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.feed.adapter.in.web.response.FeedShowMineResponse;
import konkuk.thip.feed.adapter.in.web.response.FeedShowAllResponse;
import konkuk.thip.feed.application.port.in.FeedShowAllUseCase;
import konkuk.thip.feed.application.port.in.FeedShowMineUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Feed Query API", description = "피드 조회 관련 API")
@RestController
@RequiredArgsConstructor
public class FeedQueryController {

    private final FeedShowAllUseCase feedShowAllUseCase;
    private final FeedShowMineUseCase feedShowMineUseCase;

    @Operation(
            summary = "피드 전체 조회",
            description = "사용자가 작성한 피드를 전체 조회합니다."
    )
    @GetMapping("/feeds")
    public BaseResponse<FeedShowAllResponse> showAllFeeds(
            @Parameter(hidden = true) @UserId final Long userId,
            @Parameter(description = "커서 (첫번째 요청시 : null, 다음 요청시 : 이전 요청에서 반환받은 nextCursor 값)")
            @RequestParam(value = "cursor", required = false) final String cursor) {
        return BaseResponse.ok(feedShowAllUseCase.showAllFeeds(userId, cursor));
    }

    @GetMapping("/feeds/mine")
    public BaseResponse<FeedShowMineResponse> showMyFeeds(
            @UserId final Long userId,
            @RequestParam(value = "cursor", required = false) final String cursor) {
        return BaseResponse.ok(feedShowMineUseCase.showMyFeeds(userId, cursor));
    }
}
