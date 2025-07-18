package konkuk.thip.feed.adapter.in.web;

import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.feed.adapter.in.web.request.FeedCreateRequest;
import konkuk.thip.feed.adapter.in.web.response.FeedCreateResponse;
import konkuk.thip.feed.application.port.in.FeedCreateUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FeedCommandController {

    private final FeedCreateUseCase feedCreateUseCase;

    @PostMapping("/feeds")
    public BaseResponse<FeedCreateResponse> createFeed(@RequestPart("request") @Valid final FeedCreateRequest request,
                                                       @RequestPart(value = "images", required = false) final List<MultipartFile> images,
                                                       @UserId final Long userId) {
        return BaseResponse.ok(FeedCreateResponse.of(feedCreateUseCase.createFeed(request.toCommand(userId),images)));
    }
}
