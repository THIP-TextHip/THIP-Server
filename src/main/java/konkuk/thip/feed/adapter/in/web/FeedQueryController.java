package konkuk.thip.feed.adapter.in.web;

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

@RestController
@RequiredArgsConstructor
public class FeedQueryController {

    private final FeedShowAllUseCase feedShowAllUseCase;
    private final FeedShowMineUseCase feedShowMineUseCase;

    @GetMapping("/feeds")
    public BaseResponse<FeedShowAllResponse> showAllFeeds(
            @UserId final Long userId,
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
