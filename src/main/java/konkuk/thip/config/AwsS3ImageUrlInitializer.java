package konkuk.thip.config;

import jakarta.annotation.PostConstruct;
import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.config.properties.AwsS3Properties;
import konkuk.thip.room.domain.value.Category;
import konkuk.thip.user.domain.value.Alias;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static konkuk.thip.common.exception.code.ErrorCode.AWS_BUCKET_BASE_URL_NOT_CONFIGURED;

@Component
@RequiredArgsConstructor
public class AwsS3ImageUrlInitializer {

    private final AwsS3Properties awsS3Properties;

    @PostConstruct
    void bindCloudFrontBaseUrl() {
        String baseUrl = awsS3Properties.cloudFrontBaseUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new BusinessException(AWS_BUCKET_BASE_URL_NOT_CONFIGURED);
        }

        Alias.registerBaseUrlSupplier(awsS3Properties::cloudFrontBaseUrl);
        Category.registerBaseUrlSupplier(awsS3Properties::cloudFrontBaseUrl);
    }
}
