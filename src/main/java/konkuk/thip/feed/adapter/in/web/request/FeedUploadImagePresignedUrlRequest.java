package konkuk.thip.feed.adapter.in.web.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Schema(description = "피드 생성시 이미지 업로드용 presigned url발급 요청 DTO")
public record FeedUploadImagePresignedUrlRequest(

        @Schema(description = "파일 확장자 [허용 확장자 jpg, jpeg, png, gif]", example = "png")
        @NotBlank(message = "파일 확장자는 필수입니다.")
        String extension,

        @Schema(description = "파일 크기 (바이트) [허용 파일 최대 크기 5MB]", example = "102400")
        @Positive(message = "파일 크기는 양수여야 합니다.")
        long size
) {
}
