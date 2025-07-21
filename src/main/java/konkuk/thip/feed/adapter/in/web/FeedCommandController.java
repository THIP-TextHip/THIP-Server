package konkuk.thip.feed.adapter.in.web;

import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.feed.adapter.in.web.request.FeedCreateRequest;
import konkuk.thip.feed.adapter.in.web.request.FeedUpdateRequest;
import konkuk.thip.feed.adapter.in.web.response.FeedIdResponse;
import konkuk.thip.feed.application.port.in.FeedCreateUseCase;
import konkuk.thip.feed.application.port.in.FeedUpdateUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FeedCommandController {

    private final FeedCreateUseCase feedCreateUseCase;
    private final FeedUpdateUseCase feedUpdateUseCase;

    //피드 작성
    @PostMapping("/feeds")
    public BaseResponse<FeedIdResponse> createFeed(@RequestPart("request") @Valid final FeedCreateRequest request,
                                                   @RequestPart(value = "images", required = false) final List<MultipartFile> images,
                                                   @UserId final Long userId) {
        return BaseResponse.ok(FeedIdResponse.of(feedCreateUseCase.createFeed(request.toCommand(userId),images)));
    }

    // 피드 수정 (책 빼고 변경가능)
    @PatchMapping("/feeds/{feedId}")
    public BaseResponse<FeedIdResponse> updateFeed(@RequestBody @Valid final FeedUpdateRequest request,
                                                   @PathVariable("feedId") final Long feedId,
                                                   @UserId final Long userId) {

        return BaseResponse.ok(FeedIdResponse.of(feedUpdateUseCase.updateFeed(request.toCommand(userId,feedId))));

    }
}
