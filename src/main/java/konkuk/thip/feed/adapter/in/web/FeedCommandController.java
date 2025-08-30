package konkuk.thip.feed.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.common.swagger.annotation.ExceptionDescription;
import konkuk.thip.feed.adapter.in.web.request.*;
import konkuk.thip.feed.adapter.in.web.response.FeedIdResponse;
import konkuk.thip.feed.adapter.in.web.response.FeedIsLikeResponse;
import konkuk.thip.feed.adapter.in.web.response.FeedIsSavedResponse;
import konkuk.thip.feed.adapter.in.web.response.FeedUploadImagePresignedUrlResponse;
import konkuk.thip.feed.adapter.out.s3.S3Service;
import konkuk.thip.feed.application.port.in.*;
import konkuk.thip.post.application.port.in.PostLikeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static konkuk.thip.common.swagger.SwaggerResponseDescription.*;

@Tag(name = "Feed Command API", description = "피드 상태변경 관련 API")
@RestController
@RequiredArgsConstructor
public class FeedCommandController {

    private final FeedCreateUseCase feedCreateUseCase;
    private final FeedUpdateUseCase feedUpdateUseCase;
    private final FeedSavedUseCase feedSavedUseCase;
    private final PostLikeUseCase postLikeUseCase;
    private final FeedDeleteUseCase feedDeleteUseCase;

    private final S3Service s3Service;

    @Operation(
            summary = "피드 작성",
            description = "사용자가 피드를 작성합니다."
    )
    @ExceptionDescription(FEED_CREATE)
    @PostMapping("/feeds")
    public BaseResponse<FeedIdResponse> createFeed(
            @RequestBody @Valid final FeedCreateRequest request,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(FeedIdResponse.of(feedCreateUseCase.createFeed(request.toCommand(userId))));
    }

    @Operation(
            summary = "피드 생성시 이미지 업로드용 presigned url 발급 요청",
            description = "S3에 프론트엔드가 직접 이미지를 업로드하기위한 presigned url를 발급 받습니다."
    )
    @ExceptionDescription(FEED_IMAGE_UPLOAD)
    @PostMapping("/feeds/images/presigned-url")
    public BaseResponse<FeedUploadImagePresignedUrlResponse> getPresignedUrls(@RequestBody List<FeedUploadImagePresignedUrlRequest> request) {
        return BaseResponse.ok(s3Service.getPresignedUrl(request));
    }

    @Operation(
            summary = "피드 수정",
            description = "사용자가 피드를 수정합니다. 책을 제외하고 모든 피드의 정보를 수정가능합니다.\n" +
                    "이미지는 삭제만 가능하며, 태그,이미지의 경우 수정 시 변경된 값 즉, DB에 존재해야하는 값들을 보내주시면 됩니다."
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

    @Operation(
            summary = "피드 좋아요 상태 변경",
            description = "사용자가 피드의 좋아요 상태를 변경합니다. (true -> 좋아요, false -> 좋아요 취소)"
    )
    @ExceptionDescription(CHANGE_FEED_LIKE_STATE)
    @PostMapping("/feeds/{feedId}/likes")
    public BaseResponse<FeedIsLikeResponse> likeFeed(
            @RequestBody @Valid final FeedIsLikeRequest request,
            @Parameter(description = "좋아요 상태를 변경하려는 피드 ID", example = "1")@PathVariable("feedId") final Long feedId,
            @Parameter(hidden = true) @UserId final Long userId) {
        return BaseResponse.ok(FeedIsLikeResponse.of(postLikeUseCase.changeLikeStatusPost(request.toCommand(userId, feedId))));
    }

    @Operation(
            summary = "피드 삭제",
            description = "사용자가 피드를 삭제합니다."
    )
    @ExceptionDescription(FEED_DELETE)
    @DeleteMapping("/feeds/{feedId}")
    public BaseResponse<Void> deleteFeed(
            @Parameter(description = "삭제하려는 피드 ID", example = "1") @PathVariable("feedId") final Long feedId,
            @Parameter(hidden = true) @UserId final Long userId) {
        feedDeleteUseCase.deleteFeed(feedId, userId);
        return BaseResponse.ok(null);
    }

}
