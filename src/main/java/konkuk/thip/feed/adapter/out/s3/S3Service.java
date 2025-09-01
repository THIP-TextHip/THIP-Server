package konkuk.thip.feed.adapter.out.s3;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.feed.adapter.in.web.request.FeedUploadImagePresignedUrlRequest;
import konkuk.thip.feed.adapter.in.web.response.FeedUploadImagePresignedUrlResponse;
import konkuk.thip.feed.domain.value.ContentList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static konkuk.thip.common.exception.code.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.cloud-front-base-url}")
    private String cloudFrontBaseUrl;

    private final AmazonS3 amazonS3;

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long URL_EXPIRED_TIME = 5 * 60 * 1000; // 5분

    public FeedUploadImagePresignedUrlResponse getPresignedUrl(List<FeedUploadImagePresignedUrlRequest> images, Long userId) {

        // 이미지 업로드 개수 검증
        ContentList.validateImageCount(images.size());

        List<FeedUploadImagePresignedUrlResponse.PresignedUrlInfo> result = new ArrayList<>();

        for (FeedUploadImagePresignedUrlRequest image : images) {

            // 확장자 검증
            String ext = image.extension() == null ? "" : image.extension().toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                throw new BusinessException(INVALID_FILE_EXTENSION);
            }

            // 파일 크기 검증
            if (image.size() > MAX_FILE_SIZE) {
                throw new BusinessException(FILE_SIZE_OVERFLOW);
            }

            // 현재 날짜를 yyMMdd 형식으로 포맷팅
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
            // 객체 key 생성 (일단 피드 생성시에만 이미지를 업로드하기때문에 나중에 더 추가되면 분기처리)
            String key = "feed/" + userId + "/" + datePath + "/" + UUID.randomUUID() + image.filename();

            // url 유효기간 설정하기(5분)
            Date expiration = getExpiration();

            // presigned url 생성하기
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    getPutGeneratePresignedUrlRequest(key, expiration);

            URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

            // 업로드 후 접근 가능한 파일 URL
            String fileUrl = cloudFrontBaseUrl + key;

            result.add(new FeedUploadImagePresignedUrlResponse.PresignedUrlInfo(url.toExternalForm(), fileUrl));
        }

        return FeedUploadImagePresignedUrlResponse.of(result);
    }

    // put 용 URL 생성
    private GeneratePresignedUrlRequest getPutGeneratePresignedUrlRequest(String fileName, Date expiration) {
        return new GeneratePresignedUrlRequest(bucket, fileName)
                .withMethod(HttpMethod.PUT)
                .withKey(fileName)
                .withExpiration(expiration)
                .withContentType(determineMimeTypeFromExtension(fileName));
    }

    private static Date getExpiration() {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += URL_EXPIRED_TIME;
        expiration.setTime(expTimeMillis);
        return expiration;
    }

    private String determineMimeTypeFromExtension(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            default ->  throw new BusinessException(INVALID_FILE_EXTENSION);
        };
    }


}
