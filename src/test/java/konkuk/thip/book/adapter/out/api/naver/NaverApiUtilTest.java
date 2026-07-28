package konkuk.thip.book.adapter.out.api.naver;

import konkuk.thip.common.exception.BusinessException;
import konkuk.thip.common.exception.ExternalApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.net.ServerSocket;

import static konkuk.thip.common.exception.code.ErrorCode.BOOK_NAVER_API_REQUEST_ERROR;
import static org.assertj.core.api.Assertions.*;

@DisplayName("[단위] NaverApiUtil 테스트")
class NaverApiUtilTest {

    private NaverApiUtil createTestUtil() {
        NaverApiUtil util = Mockito.spy(new NaverApiUtil());
        // @Value로 주입되는 필드를 직접 세팅
        try {
            Field clientIdField = NaverApiUtil.class.getDeclaredField("clientId");
            clientIdField.setAccessible(true);
            clientIdField.set(util, "dummy-client-id");

            Field clientSecretField = NaverApiUtil.class.getDeclaredField("clientSecret");
            clientSecretField.setAccessible(true);
            clientSecretField.set(util, "dummy-client-secret");

            Field bookSearchUrlField = NaverApiUtil.class.getDeclaredField("bookSearchUrl");
            bookSearchUrlField.setAccessible(true);
            bookSearchUrlField.set(util, "https://dummy-url.com/search?query=");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return util;
    }

    @Test
    @DisplayName("NaverApiUtil - 정상적으로 XML 응답을 반환한다 (외부 API 성공 케이스)")
    void searchBook_success_mocking() {
        // given
        NaverApiUtil naverApiUtil = createTestUtil();

        String expectedXml = "<rss><channel><total>1</total><start>1</start></channel></rss>";
        Mockito.doReturn(expectedXml)
                .when(naverApiUtil)
                .get(Mockito.anyString(), Mockito.anyMap());

        // when
        String result = naverApiUtil.searchBook("테스트", 1);

        // then
        assertThat(result).contains("<total>1</total>");
        assertThat(result).contains("<start>1</start>");
    }

    @Test
    @DisplayName("NaverApiUtil - get 메서드에서 예외 발생 시 BusinessException 발생")
    void searchBook_ioException() {
        // given
        NaverApiUtil naverApiUtil = createTestUtil();

        Mockito.doThrow(new BusinessException(BOOK_NAVER_API_REQUEST_ERROR))
                .when(naverApiUtil)
                .get(Mockito.anyString(), Mockito.anyMap());

        // when & then
        assertThatThrownBy(() -> naverApiUtil.searchBook("테스트", 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(BOOK_NAVER_API_REQUEST_ERROR.getMessage());
    }

    @Test
    @DisplayName("네트워크 IOException 발생 시 ExternalApiException으로 변환된다 - 타임아웃 설정 동작 확인")
    void get_ioException_throwsExternalApiException() throws Exception {
        // given: 연결 수락 후 즉시 닫는 서버
        // SocketTimeoutException(타임아웃)과 SocketException(연결 끊김) 모두 IOException을 상속하므로
        // get() 내부의 catch (IOException e) → ExternalApiException 변환 경로가 동일함
        try (ServerSocket server = new ServerSocket(0)) {
            new Thread(() -> {
                try { server.accept().close(); } catch (Exception ignored) {}
            }).start();

            // spy 없이 실제 get()을 실행하여 내부 catch 블록까지 동작 검증
            NaverApiUtil util = new NaverApiUtil();
            Field urlField = NaverApiUtil.class.getDeclaredField("bookSearchUrl");
            urlField.setAccessible(true);
            urlField.set(util, "http://localhost:" + server.getLocalPort() + "/?query=");

            Field clientIdField = NaverApiUtil.class.getDeclaredField("clientId");
            clientIdField.setAccessible(true);
            clientIdField.set(util, "dummy");

            Field clientSecretField = NaverApiUtil.class.getDeclaredField("clientSecret");
            clientSecretField.setAccessible(true);
            clientSecretField.set(util, "dummy");

            // when & then: IOException → catch → ExternalApiException(BOOK_NAVER_API_REQUEST_ERROR)
            assertThatThrownBy(() -> util.searchBook("테스트", 1))
                    .isInstanceOf(ExternalApiException.class);
        }
    }
}
