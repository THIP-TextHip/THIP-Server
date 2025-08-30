package konkuk.thip.feed.adapter.out.s3;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.ibm.icu.text.Transliterator;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.feed.adapter.in.web.request.FeedUploadImagePresignedUrlRequest;
import konkuk.thip.feed.adapter.in.web.response.FeedUploadImagePresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
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
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; //5MB
    private static final long URL_EXPIRED_TIME = 5 * 60 * 1000; //5분

    public FeedUploadImagePresignedUrlResponse getPresignedUrl(List<FeedUploadImagePresignedUrlRequest> images) {

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

            // 한글 파일명일 경우 영문자로 변환
            String sanitizedFilename = Transliterator.getInstance("Hangul-Latin").transliterate(image.filename());

            // UUID 앞 10자리 + 정리된 파일명으로 객체 key 생성
            String key = UUID.randomUUID().toString().substring(0, 10) + sanitizedFilename;

            // url 유효기간 설정하기(1시간)
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
                .withExpiration(expiration);
    }

    private static Date getExpiration() {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += URL_EXPIRED_TIME;
        expiration.setTime(expTimeMillis);
        return expiration;
    }
}
