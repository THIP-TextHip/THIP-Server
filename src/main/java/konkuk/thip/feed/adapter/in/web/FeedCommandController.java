package konkuk.thip.feed.adapter.in.web;

import jakarta.validation.Valid;
import konkuk.thip.common.dto.BaseResponse;
import konkuk.thip.common.exception.InvalidStateException;
import konkuk.thip.common.security.annotation.UserId;
import konkuk.thip.feed.adapter.in.web.request.FeedCreateRequest;
import konkuk.thip.feed.adapter.in.web.response.FeedCreateResponse;
import konkuk.thip.feed.adapter.out.s3.S3Service;
import konkuk.thip.feed.application.port.in.FeedCreateUseCase;
import konkuk.thip.feed.application.port.in.dto.FeedCreateCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.API_INVALID_PARAM;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FeedCommandController {

    private final FeedCreateUseCase feedCreateUseCase;
    private final S3Service s3Service;

    @PostMapping("/feeds")
    public BaseResponse<FeedCreateResponse> createFeed(@RequestPart("request") @Valid final FeedCreateRequest request,
                                                       @RequestPart(value = "images", required = false) final List<MultipartFile> images,
                                                       @UserId final Long userId) {

        // 이미지 개수 제한: 3개 초과 입력 시 예외 발생
        validateImageCount(images);

        //S3에 이미지 업로드
        List<String> imageUrls = uploadImages(images);

        FeedCreateCommand command = request.toCommand(imageUrls,userId);

        try {
            return BaseResponse.ok(FeedCreateResponse.of(feedCreateUseCase.createFeed(command)));
        } catch (Exception businessException) {
            deleteUploadedImages(imageUrls); // 비즈니스 로직 실패 시 S3에 업로드된 이미지 삭제(고아파일 방지)
            throw businessException;
        }
    }

    private void validateImageCount(List<MultipartFile> images) {
        if (images != null && images.size() > 3) {
            throw new InvalidStateException(API_INVALID_PARAM, new IllegalArgumentException("이미지는 최대 3개까지 업로드할 수 있습니다."));
        }
    }

    private List<String> uploadImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return List.of();
        return images.stream()
                .map(s3Service::getImageFromUser)
                .toList();
    }

    private void deleteUploadedImages(List<String> imageUrls) {
        for (String url : imageUrls) {
            try {
                s3Service.deleteImageFromS3(url);
            } catch (Exception e) {
                log.error("비동기 이미지 삭제 실패: {}", url, e);
            }
        }
    }

}
