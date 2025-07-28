package konkuk.thip.feed.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.feed.adapter.in.web.request.FeedCreateRequest;
import konkuk.thip.feed.adapter.in.web.request.FeedIsSavedRequest;
import konkuk.thip.feed.adapter.in.web.request.FeedUpdateRequest;
import konkuk.thip.feed.adapter.in.web.response.FeedIdResponse;
import konkuk.thip.feed.adapter.in.web.response.FeedIsSavedResponse;
import konkuk.thip.feed.application.port.in.FeedCreateUseCase;
import konkuk.thip.feed.application.port.in.FeedSavedUseCase;
import konkuk.thip.feed.application.port.in.FeedUpdateUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.*;

@Tag(name = "Feed Command API", description = "피드 상태변경 관련 API")
@RestController
@RequiredArgsConstructor
public class FeedCommandController {

    private final FeedCreateUseCase feedCreateUseCase;
    private final FeedUpdateUseCase feedUpdateUseCase;
    private final FeedSavedUseCase feedSavedUseCase;

    @Operation(
            summary = "피드 작성",
            description = "사용자가 피드를 작성합니다."
    )
    @ExceptionDescription(FEED_CREATE)
    @PostMapping("/feeds")
    public BaseResponse<FeedIdResponse> createFeed(
            @RequestPart("request") @Valid final FeedCreateRequest request,
            @Parameter(description = "피드에 첨부할 이미지 파일들") @RequestPart(value = "images", required = false) final List<MultipartFile> images,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(FeedIdResponse.of(feedCreateUseCase.createFeed(request.toCommand(userId),images)));
    }

    @Operation(
            summary = "피드 수정 (책 빼고 변경가능)",
            description = "사용자가 피드를 수정합니다."
    )
    @ExceptionDescription(FEED_UPDATE)
    @PatchMapping("/feeds/{feedId}")
    public BaseResponse<FeedIdResponse> updateFeed(
            @RequestBody @Valid final FeedUpdateRequest request,
            @Parameter(description = "수정할 피드 ID") @PathVariable("feedId") final Long feedId,
            @Parameter(hidden = true) @UserId final Long userId) {

        return BaseResponse.ok(FeedIdResponse.of(feedUpdateUseCase.updateFeed(request.toCommand(userId,feedId))));

    }

    @Operation(
            summary = "피드 저장 상태 변경",
            description = "사용자가 피드의 저장 상태를 변경합니다. 저장: true, 저장해제(삭제): false"
    )
    @ExceptionDescription(CHANGE_FEED_SAVED_STATE)
    @PostMapping("/feeds/{feedId}/saved")
    public BaseResponse<FeedIsSavedResponse> changeSavedFeed(
            @RequestBody final FeedIsSavedRequest request,
            @Parameter(description = "저장 상태 변경하려는 피드 ID") @PathVariable("feedId") final Long feedId,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(FeedIsSavedResponse.of(feedSavedUseCase.changeSavedFeed(FeedIsSavedRequest.toCommand(userId,feedId,request.type()))));
    }

}
