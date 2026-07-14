package konkuk.thip.feed.adapter.in.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import konkuk.thip.feed.adapter.out.cache.FeedCacheHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        properties = "cache.warmup.enabled=true"
)
@ActiveProfiles("test")
@DisplayName("[단위] FeedCacheWarmupListener 단위 테스트")
class FeedCacheWarmupListenerTest {

    @Autowired private ApplicationEventPublisher publisher;
    @MockitoBean private FeedCacheHandler feedCacheHandler;

    @Test
    @DisplayName("ApplicationReadyEvent 이벤트 발행 → 캐시 워밍업(ID 조회 및 상세 캐싱)이 수행된다")
    void handleContextReady_Success() {
        // 컨텍스트 로딩 시점에 자동 실행된 기록 지우기
        Mockito.clearInvocations(feedCacheHandler);

        // given
        List<Long> mockTopIds = List.of(1L, 2L, 3L);
        given(feedCacheHandler.getTopIds()).willReturn(mockTopIds);

        // when 수동으로 ApplicationReadyEvent 이벤트 발행
        publisher.publishEvent(new ApplicationReadyEvent(
                Mockito.mock(SpringApplication.class),
                new String[]{},
                Mockito.mock(ConfigurableApplicationContext.class),
                null
        ));

        // then
        verify(feedCacheHandler, times(1)).getTopIds();
        verify(feedCacheHandler, times(1)).warmUpFeedDetails(mockTopIds);
    }

    @Test
    @DisplayName("워밍업 중 예외 발생 → 애플리케이션이 종료되지 않고 로그만 남겨야 한다")
    void handleContextReady_Exception() {
        // 컨텍스트 로딩 시점에 자동 실행된 기록 지우기
        Mockito.clearInvocations(feedCacheHandler);

        // given 첫 번째 단계인 getTopIds에서 에러가 터지도록 설정
        given(feedCacheHandler.getTopIds()).willThrow(new RuntimeException("DB Connection Error"));

        // when & then: 예외가 밖으로 던져지지 않는지 확인
        assertDoesNotThrow(() -> {
            publisher.publishEvent(new ApplicationReadyEvent(
                    Mockito.mock(SpringApplication.class),
                    new String[]{},
                    Mockito.mock(ConfigurableApplicationContext.class),
                    null
            ));
        });

        // 에러가 났으므로 그 다음 단계인 상세 데이터 캐싱은 호출되지 않아야 함
        verify(feedCacheHandler, times(1)).getTopIds();
        verify(feedCacheHandler, never()).warmUpFeedDetails(anyList());
    }


}