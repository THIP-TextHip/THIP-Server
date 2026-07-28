package konkuk.thip.book.adapter.out.api;

import konkuk.thip.book.adapter.out.api.aladin.AladinApiClient;
import konkuk.thip.book.adapter.out.api.naver.NaverApiClient;
import konkuk.thip.config.WebClientConfig;
import konkuk.thip.common.discord.DiscordClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.ResourceAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

/**
 * [수동 검증용] 알라딘 API 타임아웃 시 실제 Discord 웹훅으로 알림이 전송되는지 검증하는 통합 테스트.
 *
 * - 알라딘/네이버 API: @MockitoBean으로 타임아웃 상황 재현 (실제 서버 연결 불필요)
 * - DiscordClient: application-test.yml의 webhook-url을 자동 주입받아 실제 Discord로 전송
 * - discord.env를 @TestPropertySource로 "local" 오버라이드
 *   → DiscordClient 내부의 조기 반환(if "test".equals(env) return)을 우회하여 실제 전송 활성화
 *
 * 테스트 실행 후 Discord 채널에서 "알라딘 API 페이지 정보 획득 실패" 알림 수신 여부를 직접 확인한다.
 */
@SpringBootTest(classes = {WebClientConfig.class, DiscordClient.class, CompositeBookApiAdapter.class})
@ActiveProfiles("test")
//@TestPropertySource(properties = "discord.env=local")
@DisplayName("[수동 검증] 알라딘 API 타임아웃 → 실제 Discord 알림 전송 통합 테스트")
class AladinApiDiscordAlertIntegrationTest {

    private static final String ISBN = "9791168342941";

    @MockitoBean AladinApiClient aladinApiClient;
    @MockitoBean NaverApiClient naverApiClient;

    @Autowired CompositeBookApiAdapter adapter;

    @Test
    @DisplayName("알라딘 API 타임아웃 시 실제 Discord 웹훅으로 알림이 전송되고 null이 반환된다")
    void findPageCountByIsbn_timeout_sendsRealDiscordAlert() throws InterruptedException {
        // given: RestTemplate read timeout → ResourceAccessException (SocketTimeoutException 래핑)
        given(aladinApiClient.findPageCountByIsbn(anyString()))
                .willThrow(new ResourceAccessException("Read timed out"));

        // when
        Integer result = adapter.findPageCountByIsbn(ISBN);

        // then: null 반환 → 방 생성은 계속 진행됨
        assertThat(result).isNull();

        // sendAladinApiFailureAlert()는 subscribe() 기반 비동기(fire-and-forget)로 동작하므로
        // 테스트 종료 전 HTTP 요청이 완료될 수 있도록 대기 후 Discord 채널에서 직접 확인
        Thread.sleep(2_000);
    }
}
