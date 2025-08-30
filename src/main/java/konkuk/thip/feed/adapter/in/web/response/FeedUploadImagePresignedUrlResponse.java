package konkuk.thip.feed.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record FeedUploadImagePresignedUrlResponse(
        List<PresignedUrlInfo> presignedUrls
) {
    public static FeedUploadImagePresignedUrlResponse of(List<PresignedUrlInfo> urls) {
        return new FeedUploadImagePresignedUrlResponse(urls);
    }

    @Schema(description = "Presigned URL 및 파일 접근 URL 정보")
    public record PresignedUrlInfo(

            @Schema(description = "PUT 요청을 보내기 위한 파일 업로드용 Presigned URL")
            String preSignedUrl,

            @Schema(description = "업로드 후 접근 가능한 파일 URL")
            String fileUrl
    ) {}
}