package konkuk.thip.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "cloud.aws.s3")
@Validated
public record AwsS3Properties(
        // 스프링 컨텍스트 시작 시점에 bucket-base-url의 값 유효성 검사 실행
        @NotBlank(message = "bucket-base-url 은 비어있을 수 없습니다.")
        @URL(message = "bucket-base-url 의 형식이 올바르지 않습니다.")
        String bucketBaseUrl
) { }
