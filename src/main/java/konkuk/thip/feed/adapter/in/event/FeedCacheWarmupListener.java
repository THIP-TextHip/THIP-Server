package konkuk.thip.feed.adapter.in.event;

import java.util.List;
import konkuk.thip.feed.adapter.out.cache.FeedCacheHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@ConditionalOnProperty(
        name="cache.warmup.enabled",
        havingValue = "true")
@RequiredArgsConstructor
public class FeedCacheWarmupListener {

    private final FeedCacheHandler feedCacheHandler;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void handleContextReady() {
        log.info("애플리케이션 준비 완료: 캐시 워밍업을 시작합니다.");

        try {
            // 1. 상위 ID 리스트 조회 및 인덱스 캐싱
            List<Long> topIds = feedCacheHandler.getTopIds();
            log.info("상위 ID {}개 추출 완료", topIds.size());

            // 2. 상세 데이터 일괄 캐싱
            if (!topIds.isEmpty()) {
                feedCacheHandler.warmUpFeedDetails(topIds);
            }
            log.info("총 {}개의 피드 상세 데이터 캐시 워밍업 완료", topIds.size());
        } catch (Exception e) {
            log.error("캐시 워밍업 중 오류가 발생했습니다: {}", e.getMessage());
        }
    }
}