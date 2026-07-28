package konkuk.thip.book.adapter.out.api;

import konkuk.thip.book.adapter.out.api.aladin.AladinApiClient;
import konkuk.thip.book.adapter.out.api.naver.NaverApiClient;
import konkuk.thip.common.discord.DiscordClient;
import konkuk.thip.common.exception.ExternalApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import static konkuk.thip.common.exception.code.ErrorCode.BOOK_ALADIN_API_ISBN_NOT_FOUND;
import static konkuk.thip.common.exception.code.ErrorCode.BOOK_NAVER_API_REQUEST_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@DisplayName("[단위] CompositeBookApiAdapter - 외부 API 실패 처리 검증")
class CompositeBookApiAdapterTest {

    private static final String ISBN = "9791168342941";

    private NaverApiClient naverApiClient;
    private AladinApiClient aladinApiClient;
    private DiscordClient discordClient;
    private CompositeBookApiAdapter adapter;

    @BeforeEach
    void setUp() {
        naverApiClient = mock(NaverApiClient.class);
        aladinApiClient = mock(AladinApiClient.class);
        discordClient = mock(DiscordClient.class);
        adapter = new CompositeBookApiAdapter(naverApiClient, aladinApiClient, discordClient);
    }

    @Nested
    @DisplayName("알라딘 API 성공")
    class Success {

        @Test
        @DisplayName("pageCount가 반환되고 Discord 알림이 전송되지 않는다")
        void findPageCountByIsbn_aladinSuccess_noDiscordAlert() {
            // given
            given(aladinApiClient.findPageCountByIsbn(ISBN)).willReturn(296);

            // when
            Integer pageCount = adapter.findPageCountByIsbn(ISBN);

            // then
            assertThat(pageCount).isEqualTo(296);
            then(discordClient).should(never()).sendAladinApiFailureAlert(any(), any());
        }
    }

    @Nested
    @DisplayName("알라딘 API 실패")
    class Failure {

        @Test
        @DisplayName("비즈니스 예외(ISBN 미존재) 발생 시 Discord 알림이 전송되고 null이 반환된다")
        void findPageCountByIsbn_businessException_sendsDiscordAlertAndReturnsNull() {
            // given
            ExternalApiException cause = new ExternalApiException(BOOK_ALADIN_API_ISBN_NOT_FOUND);
            given(aladinApiClient.findPageCountByIsbn(ISBN)).willThrow(cause);

            // when
            Integer pageCount = adapter.findPageCountByIsbn(ISBN);

            // then: 방 생성은 계속 진행 (null 반환)
            assertThat(pageCount).isNull();
            // Discord 알림은 정확한 isbn과 예외 객체로 호출되어야 한다
            then(discordClient).should().sendAladinApiFailureAlert(eq(ISBN), eq(cause));
        }

        @Test
        @DisplayName("읽기 타임아웃(ResourceAccessException) 발생 시 Discord 알림이 전송되고 null이 반환된다")
        void findPageCountByIsbn_readTimeout_sendsDiscordAlertAndReturnsNull() {
            // given: RestTemplate read timeout → ResourceAccessException
            ResourceAccessException timeoutEx = new ResourceAccessException("Read timed out");
            given(aladinApiClient.findPageCountByIsbn(ISBN)).willThrow(timeoutEx);

            // when
            Integer pageCount = adapter.findPageCountByIsbn(ISBN);

            // then: 알라딘 타임아웃도 Discord 알림 후 null 반환 → 방 생성 계속 진행
            assertThat(pageCount).isNull();
            then(discordClient).should().sendAladinApiFailureAlert(eq(ISBN), eq(timeoutEx));
        }

        @Test
        @DisplayName("예상치 못한 런타임 예외 발생 시에도 Discord 알림이 전송되고 null이 반환된다")
        void findPageCountByIsbn_unexpectedException_sendsDiscordAlertAndReturnsNull() {
            // given
            RuntimeException cause = new RuntimeException("Unexpected error");
            given(aladinApiClient.findPageCountByIsbn(ISBN)).willThrow(cause);

            // when
            Integer pageCount = adapter.findPageCountByIsbn(ISBN);

            // then
            assertThat(pageCount).isNull();
            then(discordClient).should().sendAladinApiFailureAlert(eq(ISBN), eq(cause));
        }
    }

    // ────────────────────────────────────────────────────────────────────────────────
    // 네이버 API 실패: 알라딘과 달리 예외가 그대로 전파됨 (방 생성 실패)
    // ────────────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("네이버 API 실패")
    class NaverFailure {

        @Test
        @DisplayName("네이버 API 예외 발생 시 예외가 그대로 전파되고 Discord 알림은 전송되지 않는다")
        void loadBookWithPageByIsbn_naverFails_exceptionPropagates_noDiscordAlert() {
            // given: 네이버 타임아웃/에러 발생 (HttpURLConnection IOException → ExternalApiException)
            ExternalApiException naverEx = new ExternalApiException(BOOK_NAVER_API_REQUEST_ERROR);
            given(naverApiClient.findDetailBookByIsbn(ISBN)).willThrow(naverEx);

            // when & then: 알라딘과 달리 catch 없이 예외 전파 → 방 생성 실패
            assertThatThrownBy(() -> adapter.loadBookWithPageByIsbn(ISBN))
                    .isInstanceOf(ExternalApiException.class);

            // 네이버 실패는 Discord 알림 대상이 아님 (알라딘 실패만 알림)
            then(discordClient).should(never()).sendAladinApiFailureAlert(any(), any());
        }
    }
}
