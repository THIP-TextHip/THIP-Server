package konkuk.thip.common.s3.service;

import konkuk.thip.common.annotation.HelperService;
import konkuk.thip.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

import static konkuk.thip.common.exception.code.ErrorCode.URL_INVALID_DOMAIN;
import static konkuk.thip.common.exception.code.ErrorCode.URL_USER_ID_MISMATCH;

@HelperService
@RequiredArgsConstructor
public class ImageUrlValidationService {

    @Value("${cloud.aws.s3.cloud-front-base-url}")
    private String cloudFrontBaseUrl;

    public void validateUrlDomainAndUser(List<String> imageUrls, Long currentUserId) {

        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        for (String url : imageUrls) {
            // 1. 도메인 시작 주소 확인
            if (!url.startsWith(cloudFrontBaseUrl)) {
                throw new BusinessException(URL_INVALID_DOMAIN);
            }
            // 2. 도메인 이후의 key 추출
            String key = url.substring(cloudFrontBaseUrl.length());
            // 3. key를 '/'로 분리
            String[] parts = key.split("/");

            // 4. 경로 구조 확인
            // feed/{userId}/{date}/{uuid + filename}
            if (parts.length != 4) {
                throw new BusinessException(URL_INVALID_DOMAIN);
            }

            // 5. userId와 비교
            String userIdInUrl = parts[1];
            Long userIdFromUrl = Long.parseLong(userIdInUrl);
            if (!userIdFromUrl.equals(currentUserId)) {
                throw new BusinessException(URL_USER_ID_MISMATCH);
            }
        }
    }
}
