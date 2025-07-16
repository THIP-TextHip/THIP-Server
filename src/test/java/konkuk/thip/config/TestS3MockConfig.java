package konkuk.thip.config;

import konkuk.thip.feed.adapter.out.s3.S3Service;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestS3MockConfig {

    @Bean
    public S3Service s3Service() {
        S3Service mockS3Service = Mockito.mock(S3Service.class);

        // 필요한 메서드 Mock
        Mockito.when(mockS3Service.getImageFromUser(Mockito.any()))
                .thenAnswer(invocation -> {
                    // 실제로는 업로드된 URL을 반환해야 함
                    // 가짜 URL 반환
                    return "https://mock-s3-bucket/fake-image-url.jpg";
                });

        Mockito.doNothing().when(mockS3Service).deleteImageFromS3(Mockito.anyString());

        return mockS3Service;
    }
}
