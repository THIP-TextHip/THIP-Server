package konkuk.thip.util;

import konkuk.thip.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static konkuk.thip.common.exception.code.ErrorCode.BOOK_NAVER_API_REQUEST_ERROR;
import static org.assertj.core.api.Assertions.*;

class NaverApiUtilTest {

    @Test
    @DisplayName("정상 응답을 모킹하여 반환")
    void searchBook_success_mocking() {
        // given
        NaverApiUtil naverApiUtil = Mockito.spy(new NaverApiUtil());

        // get 메서드를 spy로 모킹 (실제 네트워크 호출 없이 원하는 응답 반환)
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
    @DisplayName("get 메서드에서 예외가 발생하면 BusinessException이 발생")
    void searchBook_ioException() {
        // given
        NaverApiUtil naverApiUtil = Mockito.spy(new NaverApiUtil());

        // get 메서드가 예외를 던지도록 설정
        Mockito.doThrow(new BusinessException(BOOK_NAVER_API_REQUEST_ERROR))
                .when(naverApiUtil)
                .get(Mockito.anyString(), Mockito.anyMap());

        // when & then
        assertThatThrownBy(() -> naverApiUtil.searchBook("테스트", 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(BOOK_NAVER_API_REQUEST_ERROR.getMessage());
    }
}
